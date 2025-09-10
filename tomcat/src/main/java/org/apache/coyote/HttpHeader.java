package org.apache.coyote;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpHeader {
    private final List<String> headerLines;
    private final HttpCookie httpCookie;

    public HttpHeader(List<String> headerLines) {
        this.headerLines = headerLines;
        this.httpCookie = initCookie();
    }

    public String getCookie(String key) {
        return URLDecoder.decode(httpCookie.get(key), StandardCharsets.UTF_8);
    }

    public boolean hasCookie(String... keys) {
        for (String key : keys) {
            if (!httpCookie.has(key)) {
                return false;
            }
        }

        return true;
    }

    private HttpCookie initCookie() {
        if (!containsHeader("Cookie")) {
            return new HttpCookie();
        }
        String cookieHeaderValue = getFromHeader("Cookie");
        return new HttpCookie(cookieHeaderValue);
    }

    private boolean containsHeader(String... headerKeys) {
        for (String headerKey : headerKeys) {
            for (String line : headerLines) {
                if (line.regionMatches(true, 0, headerKey + ":", 0, headerKey.length() + 1)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getFromHeader(String headerKey) {
        for (String line : headerLines) {
            if (line.regionMatches(true, 0, headerKey + ":", 0, headerKey.length() + 1)) {
                return line.substring(line.indexOf(':') + 1).trim();
            }
        }

        throw new IllegalArgumentException("Header에서 다음 key를 찾을 수 없습니다. " + headerKey);
    }
}
