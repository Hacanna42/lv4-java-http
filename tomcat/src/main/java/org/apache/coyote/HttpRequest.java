package org.apache.coyote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private final List<String> requests;

    private final String path;
    private final String uri;
    private final QueryString queryString;

    public HttpRequest(InputStream inputStream) {
        this.requests = readInputStream(inputStream);

        this.path = initPath();
        this.uri = initUri();
        this.queryString = initQueryString();
    }

    private List<String> readInputStream(InputStream inputStream) {
        List<String> lines = new ArrayList<>();
        var reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    break;
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return lines;
    }

    private String initPath() {
        String initialUri = initUri();
        int index = initialUri.indexOf("?");
        if (index == -1) {
            return initialUri;
        }

        return initialUri.substring(0, index);
    }

    private String initUri() {
        return this.requests.getFirst().split(" ")[1];
    }

    private QueryString initQueryString() {
        return new QueryString(getUri());
    }

    public String getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }

    public QueryString getQueryString() {
        return queryString.copy();
    }
}
