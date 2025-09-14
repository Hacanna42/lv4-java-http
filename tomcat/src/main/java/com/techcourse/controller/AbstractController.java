package com.techcourse.controller;

import java.io.IOException;

import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;

public abstract class AbstractController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        try {
            if (request.isGet()) {
                doGet(request, response);
            } else if (request.isPost()) {
                doPost(request, response);
            } else {
                response.setStatusCode(405);
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            throw new IOException(e);
        }
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception { /* NOOP */ }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception { /* NOOP */ }
}

