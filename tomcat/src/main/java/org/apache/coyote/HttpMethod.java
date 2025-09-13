package org.apache.coyote;

import java.util.List;

public enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT, PATCH;

    public static HttpMethod from(List<String> requestLines) {
        String parsedMethod = requestLines.getFirst().split("\\s+")[0];
        return HttpMethod.valueOf(parsedMethod);
    }
}
