package org.apache.coyote.http11;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpQueryString {
    private final Map<String, String> parameters;

    public HttpQueryString(String uri) {
        parameters = parseQueryString(uri);
    }

    private HttpQueryString(Map<String, String> parameters) {
        this.parameters = Map.copyOf(parameters);
    }

    public HttpQueryString copy() {
        return new HttpQueryString(this.parameters);
    }

    public boolean has(String... keys) {
        for (String key : keys) {
            if (!parameters.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public String get(String key) {
        if (!parameters.containsKey(key)) {
            throw new IllegalArgumentException("QueryString에서 다음 key를 찾을 수 없습니다. " + key);
        }
        String value = parameters.get(key);
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQueryString(String uri) {
        int index = uri.indexOf("?");
        if (index == -1) {
            return Map.of();
        }

        Map<String, String> parsedQuery = new HashMap<>();
        String[] queries = uri.substring(index + 1).split("&");
        for (String query : queries) {
            String key = query.split("=", 2)[0];
            String value = query.split("=", 2)[1];
            parsedQuery.put(key, value);
        }

        return parsedQuery;
    }
}
