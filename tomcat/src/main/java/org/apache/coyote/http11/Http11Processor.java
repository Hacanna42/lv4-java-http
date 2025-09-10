package org.apache.coyote.http11;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.apache.coyote.HttpBody;
import org.apache.coyote.HttpRequest;
import org.apache.coyote.HttpResponse;
import org.apache.coyote.Processor;
import org.apache.coyote.QueryString;
import org.apache.coyote.Session;
import org.apache.coyote.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;
    private final SessionManager sessionManager;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
        this.sessionManager = SessionManager.getInstance();
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

            HttpResponse httpResponse = new HttpResponse(outputStream);

            // Content-Type Handling
            determineContentType(httpResponse, requestPath);

            // Location Handling - /login
            if (requestPath.equals("/login") && httpRequest.isGet()) {
                requestPath = "/login.html";

                User user = getUserFromJSession(httpRequest);
                if (user != null) {
                    httpResponse.setStatusCode(302);
                    requestPath = "/index.html";
                }
            }

            if (requestPath.equals("/login") && httpRequest.isPost()) {
                HttpBody httpBody = httpRequest.getBody();
                if (httpBody.hasKey("account", "password")) {
                    String account = httpBody.getValue("account");
                    String password = httpBody.getValue("password");
                    User user = InMemoryUserRepository.findByAccount(account)
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. " + account));
                    boolean login = user.checkPassword(password);
                    if (login) {
                        addJSession(httpResponse, user);
                        System.out.println(user);
                        httpResponse.setStatusCode(302);
                        requestPath = "/index.html";
                    } else {
                        requestPath = "/401.html";
                    }
                } else {
                    requestPath = "/401.html";
                }
            }

            // Location Handling - /register
            if (requestPath.equals("/register") && httpRequest.isGet()) {
                requestPath = "/register.html";
            }
            if (requestPath.equals("/register") && httpRequest.isPost()) {
                HttpBody httpBody = httpRequest.getBody();
                if (httpBody.hasKey("account", "email", "password")) {
                    String account = httpBody.getValue("account");
                    String email = httpBody.getValue("email");
                    String password = httpBody.getValue("password");
                    User user = new User(account, password, email);
                    Optional<User> foundUser = InMemoryUserRepository.findByAccount(account);
                    if (foundUser.isPresent()) {
                        throw new IllegalArgumentException("이미 존재하는 계정입니다. " + account);
                    }

                    InMemoryUserRepository.save(user);
                    addJSession(httpResponse, user);
                    System.out.println("회원가입 성공: " + user);
                    requestPath = "/index.html";
                    httpResponse.setStatusCode(302);
                } else {
                    requestPath = "/register.html";
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

    private User getUserFromJSession(HttpRequest httpRequest) {
        if (httpRequest.hasCookie("JSESSIONID")) {
            String jSessionId = httpRequest.getCookie("JSESSIONID");
            Session session = sessionManager.find(jSessionId);
            if (session != null) {
                return (User) session.getAttribute("user");
            }
        }

        return null;
    }

    private void addJSession(HttpResponse httpResponse, User user) {
        String jSessionId = UUID.randomUUID().toString();
        httpResponse.addCookie("JSESSIONID", jSessionId);
        Session session = new Session(jSessionId);
        session.setAttribute("user", user);
        sessionManager.add(session);
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

    private String readStaticFile(String path) throws IOException, URISyntaxException {
        if (path.equals("/")) {
            return null;
        }

        final URL resource = getClass().getClassLoader().getResource("static" + path);
        if (resource == null) {
            throw new IllegalArgumentException("해당 파일을 찾을 수 없습니다:" + path);
        }
        return new String(Files.readAllBytes(Paths.get(resource.toURI())));
    }
}
