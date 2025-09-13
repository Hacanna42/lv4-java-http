package org.apache.coyote;

import java.util.List;

public enum HttpVersion {
    HTTP_1_0(1.0),
    HTTP_1_1(1.1),
    HTTP_2_0(2.0);

    private final double version;

    HttpVersion(double version) {
        this.version = version;
    }

    public static HttpVersion from(List<String> requestLines) {
        String protocolVersion = requestLines.getFirst().trim().split("\\s+")[2];
        return switch (protocolVersion) {
            case "HTTP/1.0" -> HTTP_1_0;
            case "HTTP/1.1" -> HTTP_1_1;
            case "HTTP/2.0" -> HTTP_2_0;
            default -> throw new IllegalArgumentException("지원하지 않는 HTTP 버전: " + protocolVersion);
        };
    }
}
