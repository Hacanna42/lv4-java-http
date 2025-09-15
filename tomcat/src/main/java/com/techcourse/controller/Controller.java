package com.techcourse.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.coyote.http11.HttpRequest;
import org.apache.coyote.http11.HttpResponse;

public interface Controller {
    void service(HttpRequest request, HttpResponse response) throws IOException, URISyntaxException;
}
