package com.techcourse.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.http11.HttpRequest;

public class RequestMapping {
    private static final RequestMapping instance = new RequestMapping();
    private final Map<String, Controller> mappings = new HashMap<>();
    private final StaticResourceController staticResourceController = new StaticResourceController();

    private RequestMapping() {
        mappings.put("/register", new RegisterController());
        mappings.put("/login", new LoginController());
    }

    public static RequestMapping getInstance() {
        return instance;
    }

    public Controller getController(HttpRequest request) {
        String path = request.getPath();
        if (mappings.containsKey(path)) {
            return mappings.get(path);
        }

        if (isStaticResource(path)) {
            return staticResourceController;
        }
        return null;
    }

    private boolean isStaticResource(String path) {
        return path.equals("/") ||
               path.matches(".*\\.(html|htm|css|js|json|xml|jpg|jpeg|png|gif|svg|ico|woff|woff2)$");
    }
}
