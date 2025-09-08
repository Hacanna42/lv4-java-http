package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {

            List<String> request = getRequest(inputStream);
            String requestPath = getPathFromRequest(request);
            Map<String, String> requestQueries = getQueriesFromRequest(request);

            if (requestPath.equals("/login")) {
                String account = requestQueries.get("account");
                User user = InMemoryUserRepository.findByAccount(account).orElseThrow();
                boolean login = user.checkPassword(requestQueries.get("password"));
                System.out.println(user);
            }
            String content = readStaticFile(requestPath);

            if (content == null) {
                content = "Hello world!";
            }

            String response = parse200Response(requestPath, content);
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException | URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getPathFromRequest(List<String> request) {
        String uri = getUriFromRequest(request);
        int index = uri.indexOf("?");
        if (index == -1) {
            return uri;
        }

        return uri.substring(0, index);
    }

    private Map<String, String> getQueriesFromRequest(List<String> request) {
        String uri = getUriFromRequest(request);
        int index = uri.indexOf("?");
        if (index == -1) {
            return null;
        }
        String path = uri.substring(0, index);
        String queryString = uri.substring(index + 1);
        String[] keyValue = queryString.split("&");
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < keyValue.length; i++) {
            String cur = keyValue[i];
            String key = cur.split("=")[0];
            String value = cur.split("=")[1];
            map.put(key, value);
        }

        return map;
    }

    private String parse200Response(String requestUri, String content) {
        String contentType = "text/html;charset=utf-8";
        if (requestUri.endsWith(".css"))
            contentType = "text/css;charset=utf-8";
        return String.join("\r\n",
            "HTTP/1.1 200 OK",
            "Content-Type: " + contentType,
            "Content-Length: " + content.getBytes().length,
            "",
            content);
    }

    private String readStaticFile(String requestUri) throws IOException, URISyntaxException {
        if (requestUri.equals("/")) {
            return null;
        }

        if (requestUri.equals("/login")) {
            requestUri = "/login.html";
        }

        final URL resource = getClass().getClassLoader().getResource("static" + requestUri);
        return new String(Files.readAllBytes(Paths.get(resource.toURI())));
    }

    private String getUriFromRequest(List<String> request) {
        return request.getFirst().split(" ")[1];
    }

    private List<String> getRequest(InputStream inputStream) throws IOException {
        List<String> request = new ArrayList<>();
        var reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty())
                break;
            request.add(line);
        }
        return request;
    }
}
