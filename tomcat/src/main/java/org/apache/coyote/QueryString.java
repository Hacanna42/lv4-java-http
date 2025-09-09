package org.apache.coyote;

import java.util.HashMap;
import java.util.Map;

public class QueryString {
    private final Map<String, String> parameters;

    public QueryString(String uri) {
        parameters = parseQueryString(uri);
    }

    private QueryString(Map<String, String> parameters) {
        this.parameters = Map.copyOf(parameters);
    }

    public QueryString copy() {
        return new QueryString(this.parameters);
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

        return parameters.get(key);
    }

    private Map<String, String> parseQueryString(String uri) {
        int index = uri.indexOf("?");
        if (index == -1) {
            return Map.of();
        }

        Map<String, String> parsedQuery = new HashMap<>();
        String[] queries = uri.substring(index + 1).split("&");
        for (String query : queries) {
            String key = query.split("=")[0];
            String value = query.split("=")[1];
            parsedQuery.put(key, value);
        }

        return parsedQuery;
    }
}
