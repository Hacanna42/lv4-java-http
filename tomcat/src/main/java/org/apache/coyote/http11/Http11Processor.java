package org.apache.coyote.http11;

import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;

import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.controller.Controller;
import com.techcourse.controller.RequestMapping;
import com.techcourse.exception.UncheckedServletException;

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

            HttpRequest httpRequest = new HttpRequest(inputStream);
            HttpResponse httpResponse = new HttpResponse(outputStream);
            determineContentType(httpResponse, httpRequest.getPath());

            Controller controller = RequestMapping.getInstance().getController(httpRequest);
            if (controller != null) {
                controller.service(httpRequest, httpResponse);
            } else {
                httpResponse.setStatusCode(404);
                httpResponse.response("Not Found");
            }
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
}
