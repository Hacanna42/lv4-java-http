package org.apache.coyote.http11;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.coyote.HttpRequest;
import org.apache.coyote.HttpResponse;
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
            HttpResponse httpResponse = new HttpResponse(outputStream);

            String requestPath = httpRequest.getPath();
            QueryString queryString = httpRequest.getQueryString();

            // Content-Type Handling
            determineContentType(httpResponse, requestPath);

            // Location Handling - /login
            if (requestPath.equals("/login")) {
                if (queryString.has("account", "password")) {
                    String account = queryString.get("account");
                    String password = queryString.get("password");
                    User user = InMemoryUserRepository.findByAccount(account)
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. " + account));
                    boolean login = user.checkPassword(password);
                    if (login) {
                        System.out.println(user);
                        httpResponse.setStatusCode(302);
                        requestPath = "/index.html";
                    } else {
                        requestPath = "/401.html";
                    }
                } else {
                    // 로그인 정보가 없으므로 그냥 넘김
                }
            }

            String content = readStaticFile(requestPath);
            if (content == null) {
                content = "Hello world!";
            }

            // HTTP Response
            httpResponse.response(content);
        } catch (IOException | UncheckedServletException | URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void determineContentType(HttpResponse httpResponse, String requestPath) {
        if (requestPath.endsWith(".css")) {
            httpResponse.setExtension("css");
        } else if (requestPath.endsWith(".js")) {
            httpResponse.setExtension("js");
        } else if (requestPath.endsWith(".html")) {
            httpResponse.setExtension("html");
        } else if (requestPath.endsWith(".ico")) {
            httpResponse.setExtension("ico");
        } else if (requestPath.endsWith(".png")) {
            httpResponse.setExtension("png");
        } else if (requestPath.endsWith(".jpg") || requestPath.endsWith(".jpeg")) {
            httpResponse.setExtension("jpeg");
        } else if (requestPath.endsWith(".svg")) {
            httpResponse.setExtension("svg");
        } else {
            httpResponse.setExtension("html");
        }
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
