package org.apache.coyote.http11;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.coyote.HttpRequest;
import org.apache.coyote.Processor;
import org.apache.coyote.QueryString;
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

            // Request Parsing
            HttpRequest httpRequest = new HttpRequest(inputStream);
            String requestPath = httpRequest.getPath();
            QueryString queryString = httpRequest.getQueryString();

            // Location Handling
            if (requestPath.equals("/login")) {
                String account = queryString.get("account");
                User user =
                    InMemoryUserRepository.findByAccount(account).orElseThrow(() -> new IllegalArgumentException("해당 "
                        + "유저를 찾을 수 없습니다. " + account));
                boolean login = user.checkPassword(queryString.get("password"));
                System.out.println(user);
            }

            String content = readStaticFile(requestPath);
            if (content == null) {
                content = "Hello world!";
            }

            // HTTP Response
            String response = parse200Response(requestPath, content);
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException | URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
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
        if (resource == null) {
            throw new IllegalArgumentException("해당 파일을 찾을 수 없습니다:" + requestUri);
        }
        return new String(Files.readAllBytes(Paths.get(resource.toURI())));
    }
}
