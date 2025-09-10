package org.apache.coyote;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpResponse {

    private final OutputStream outputStream;
    private final List<String> headers;
    private String extension;
    private int statusCode;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.headers = new ArrayList<>();
        this.statusCode = 200;
    }

    public void response(String content) throws IOException {
        String response = parseResponse(content);

        outputStream.write(response.getBytes());
        outputStream.flush();
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void addCookie(String key, String value) {
        this.headers.add("Set-Cookie: " + key + "=" + value);
    }

    private String parseResponse(String content) {
        final String statusCodeLine = switch (this.statusCode) {
            case 100 -> "HTTP/1.1 100 Continue";
            case 101 -> "HTTP/1.1 101 Switching Protocols";
            case 200 -> "HTTP/1.1 200 OK";
            case 201 -> "HTTP/1.1 201 Created";
            case 202 -> "HTTP/1.1 202 Accepted";
            case 204 -> "HTTP/1.1 204 No Content";
            case 301 -> "HTTP/1.1 301 Moved Permanently";
            case 302 -> "HTTP/1.1 302 Found";
            case 304 -> "HTTP/1.1 304 Not Modified";
            case 400 -> "HTTP/1.1 400 Bad Request";
            case 401 -> "HTTP/1.1 401 Unauthorized";
            case 403 -> "HTTP/1.1 403 Forbidden";
            case 404 -> "HTTP/1.1 404 Not Found";
            case 405 -> "HTTP/1.1 405 Method Not Allowed";
            case 500 -> "HTTP/1.1 500 Internal Server Error";
            case 502 -> "HTTP/1.1 502 Bad Gateway";
            case 503 -> "HTTP/1.1 503 Service Unavailable";
            case 504 -> "HTTP/1.1 504 Gateway Timeout";
            default -> "HTTP/1.1 " + statusCode + " Unknown Status";
        };

        final String contentTypeLine = switch (extension) {
            case "html" -> "text/html;charset=utf-8";
            case "htm" -> "text/html;charset=utf-8";
            case "css" -> "text/css;charset=utf-8";
            case "js" -> "application/javascript;charset=utf-8";
            case "json" -> "application/json;charset=utf-8";
            case "xml" -> "application/xml;charset=utf-8";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "woff" -> "font/woff";
            case "woff2" -> "font/woff2";
            default -> "text/plain;charset=utf-8";
        };

        headers.add("Content-Type: " + contentTypeLine);
        headers.add("Content-Length: " + content.getBytes().length);

        String headerSection = String.join("\r\n", headers);

        return String.join(
            "\r\n",
            statusCodeLine,
            headerSection,
            "",
            content
        );
    }
}
