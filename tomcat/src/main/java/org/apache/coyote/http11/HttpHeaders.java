package org.apache.coyote.http11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHeaders {
    private final Map<String, String> headers;

    public HttpHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public static HttpHeaders from(List<String> requestLines) {
        List<String> headerLines = requestLines.stream().skip(1).takeWhile(line -> !line.isEmpty()).toList();
        Map<String, String> headerMap = headerLines.stream()
                .map(line -> line.split(":", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> parts[1].trim(),
                        (existing, replacement) -> existing
                ));

        return new HttpHeaders(headerMap);
    }

    public String getCookie(String key) {
        if (!headers.containsKey("Cookie")) {
            return null;
        }
        String cookieHeader = headers.get("Cookie");
        Map<String, String> parsedCookies = new HashMap<>();
        String[] keyValues = cookieHeader.split("; ");
        for (String keyValue : keyValues) {
            String[] parts = keyValue.split("=", 2);
            if (parts.length == 2) {
                parsedCookies.put(parts[0], parts[1]);
            }
        }

        return parsedCookies.getOrDefault(key, null);
    }

    public boolean hasCookie(String... keys) {
        for (String key : keys) {
            if (getCookie(key) == null) {
                return false;
            }
        }

        return true;
    }
}
