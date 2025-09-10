package org.apache.coyote;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private final String request;
    private final HttpHeader header;
    private final HttpBody body;

    private final QueryString queryString;
    private final String method;
    private final String path;
    private final String uri;

    public HttpRequest(InputStream inputStream) {
        List<String> rawInput = readInputStream(inputStream);
        this.request = rawInput.getFirst();
        this.header = new HttpHeader(rawInput.stream().skip(1).takeWhile(line -> !line.isEmpty()).toList());
        this.body = new HttpBody(rawInput.stream().dropWhile(line -> !line.isEmpty()).skip(1).toList());

        this.path = initPath();
        this.uri = initUri();
        this.queryString = initQueryString();
        this.method = initMethod();
    }

    public boolean hasCookie(String... keys) {
        return header.hasCookie(keys);
    }

    public String getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }

    public QueryString getQueryString() {
        return queryString.copy();
    }

    public boolean isGet() {
        return this.method.equals("GET");
    }

    public boolean isPost() {
        return this.method.equals("POST");
    }

    public HttpBody getBody() {
        return body;
    }

    private List<String> readInputStream(InputStream inputStream) {
        List<String> lines = new ArrayList<>();
        try {
            var reader = new BufferedReader(
                new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.US_ASCII));

            String line;
            int contentLength = 0;

            // 1) 헤더 읽기
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (line.isEmpty())
                    break; // 빈 줄 = 헤더 끝

                if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
                    contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
                }
            }

            // 2) 바디 읽기
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int off = 0;
                while (off < contentLength) {
                    int r = reader.read(bodyChars, off, contentLength - off);
                    if (r == -1)
                        break;
                    off += r;
                }

                lines.add(new String(bodyChars, 0, off));
            }

            return List.copyOf(lines);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String initPath() {
        String initialUri = initUri();
        int index = initialUri.indexOf("?");
        if (index == -1) {
            return initialUri;
        }

        return initialUri.substring(0, index);
    }

    private String initUri() {
        return this.request.split(" ")[1];
    }

    private QueryString initQueryString() {
        return new QueryString(getUri());
    }

    private String initMethod() {
        return this.request.split(" ")[0];
    }
}
