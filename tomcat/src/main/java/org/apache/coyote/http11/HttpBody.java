package org.apache.coyote.http11;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpBody {
    private final List<String> bodyLines;

    // for x-www-form-urlencoded parsing
    private final Map<String, String> formData;

    public HttpBody(List<String> bodyLines) {
        this.bodyLines = bodyLines;
        this.formData = parseFormData(bodyLines);
    }

    public static HttpBody from(List<String> requestLines) {
        return new HttpBody(requestLines.stream().dropWhile(line -> !line.isEmpty()).skip(1).toList());
    }

    public boolean hasKey(String... keys) {
        for (String key : keys) {
            if (!formData.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public String getValue(String key) {
        if (!formData.containsKey(key)) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        String value = formData.get(key);
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseFormData(List<String> bodyLines) {
        Map<String, String> parameters = new HashMap<>();
        if (bodyLines.isEmpty()) {
            return parameters;
        }

        String body = bodyLines.getFirst();
        String[] keyValues = body.split("&");
        for (String keyValue : keyValues) {
            String[] parts = keyValue.split("=", 2);
            if (parts.length == 2) {
                parameters.put(parts[0], parts[1]);
            }
        }

        return parameters;
    }
}
