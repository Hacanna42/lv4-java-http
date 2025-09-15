package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private final HttpMethod method;
    private final HttpPath path;
    private final HttpVersion version;
    private final HttpHeaders headers;
    private final HttpBody body;

    public HttpRequest(InputStream inputStream) {
        List<String> requestLines = readInputStream(inputStream);
        this.method = HttpMethod.from(requestLines);
        this.path = HttpPath.from(requestLines);
        this.version = HttpVersion.from(requestLines);
        this.headers = HttpHeaders.from(requestLines);
        this.body = HttpBody.from(requestLines);
    }

    public String getCookie(String key) {
        return headers.getCookie(key);
    }

    public boolean hasCookie(String... keys) {
        return headers.hasCookie(keys);
    }

    public String getUri() {
        return path.getUri();
    }

    public String getPath() {
        return path.getPath();
    }

    public boolean isGet() {
        return this.method == HttpMethod.GET;
    }

    public boolean isPost() {
        return this.method == HttpMethod.POST;
    }

    public HttpBody getBody() {
        return body;
    }

    private List<String> readInputStream(InputStream inputStream) {
        List<String> lines = new ArrayList<>();
        var reader = new BufferedReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.US_ASCII));
        try {
            int contentLength = readHeaders(reader, lines);
            readBody(reader, lines, contentLength);
            return List.copyOf(lines);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private int readHeaders(BufferedReader reader, List<String> lines) throws Exception {
        String line;
        int contentLength = 0;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
            if (line.isEmpty()) {
                break; // 빈 줄 = 헤더 끝
            }
            if (line.regionMatches(true, 0, "Content-Length:", 0, "Content-Length:".length())) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            }
        }

        return contentLength;
    }

    private void readBody(BufferedReader reader, List<String> lines, int contentLength) throws Exception {
        if (contentLength <= 0) {
            return;
        }
        char[] bodyChars = new char[contentLength];
        int offset = 0;
        while (offset < contentLength) {
            int r = reader.read(bodyChars, offset, contentLength - offset);
            if (r == -1)
                break;
            offset += r;
        }

        lines.add(new String(bodyChars, 0, offset));
    }
}
