package org.apache.coyote;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpCookie {
    private final Map<String, String> cookies;

    public HttpCookie() {
        this.cookies = Map.of();
    }
    public HttpCookie(String cookieHeaderValue) {
        this.cookies = parseCookies(cookieHeaderValue);
    }

    public boolean has(String... keys) {
        for (String key : keys) {
            if (!cookies.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public String get(String key) {
        if (!cookies.containsKey(key)) {
            throw new IllegalArgumentException("Cookie에서 다음 key를 찾을 수 없습니다. " + key);
        }

        String value = cookies.get(key);
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseCookies(String cookieHeaderValue) {
        Map<String, String> parsedCookies = new HashMap<>();
        String[] keyValues = cookieHeaderValue.split("; ");
        for (String keyValue : keyValues) {
            String[] parts = keyValue.split("=");
            if (parts.length == 2) {
                parsedCookies.put(parts[0], parts[1]);
            }
        }

        return parsedCookies;
    }
}
