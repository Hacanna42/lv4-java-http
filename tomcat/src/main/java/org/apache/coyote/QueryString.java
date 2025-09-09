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

    public String get(String key) {
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
