package org.apache.coyote;

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
}
