package com.techcourse.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;

public class StaticResourceController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException, URISyntaxException {
        String path = request.getPath();
        if (path.equals("/")) {
            response.response("Hello world!");
            return;
        }

        final URL resource = getClass().getClassLoader().getResource("static" + path);
        if (resource == null) {
            response.setStatusCode(404);
            response.response("File not found: " + path);
            return;
        }
        String content = new String(Files.readAllBytes(Paths.get(resource.toURI())));
        response.response(content);
    }
}
