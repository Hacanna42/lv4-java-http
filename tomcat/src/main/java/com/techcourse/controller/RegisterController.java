package com.techcourse.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;
import org.apache.coyote.http11.HttpBody;
import org.apache.coyote.session.Session;
import org.apache.coyote.session.SessionManager;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.model.User;

public class RegisterController extends AbstractController {

    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException, URISyntaxException {
        String content = readStaticFile("/register.html");
        response.response(content);
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        HttpBody httpBody = request.getBody();
        if (httpBody.hasKey("account", "email", "password")) {
            String account = httpBody.getValue("account");
            String email = httpBody.getValue("email");
            String password = httpBody.getValue("password");

            Optional<User> foundUser = InMemoryUserRepository.findByAccount(account);
            if (foundUser.isPresent()) {
                response.setStatusCode(400);
                response.response("이미 존재하는 계정입니다: " + account);
                return;
            }

            User user = new User(account, password, email);
            InMemoryUserRepository.save(user);

            addJSession(response, user);
            response.setStatusCode(302);
            response.response(readStaticFile("/index.html"));
        } else {
            response.setStatusCode(400);
            response.response("필수 입력값이 누락되었습니다.");
        }
    }

    private void addJSession(HttpResponse httpResponse, User user) {
        String jSessionId = UUID.randomUUID().toString();
        httpResponse.addCookie("JSESSIONID", jSessionId);
        Session session = new Session(jSessionId);
        session.setAttribute("user", user);
        sessionManager.add(session);
    }

    private String readStaticFile(String path) throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("static" + path);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return new String(Files.readAllBytes(Paths.get(resource.toURI())));
    }
}
