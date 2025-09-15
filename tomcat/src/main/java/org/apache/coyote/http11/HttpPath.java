package org.apache.coyote.http11;

import java.util.List;

public class HttpPath {
    private final String uri;
    private final String path;

    public HttpPath(String uri, String path) {
        this.uri = uri;
        this.path = path;
    }

    public static HttpPath from(List<String> requestLines) {
        // parse URI
        String[] parts = requestLines.getFirst().trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("잘못된 요청 라인: " + requestLines.getFirst());
        }

        String uri = parts[1];

        // parse Path
        if (!uri.contains("?")) {
            return new HttpPath(uri, uri);
        }

        return new HttpPath(uri, uri.substring(0, uri.indexOf("?")));
    }

    public String getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }
}
