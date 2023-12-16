package projects.sensor.api.verticle;

import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.App;

public abstract class RestServerVerticle extends AbstractVerticle {

    private static final int DEFAULT_PORT = 9090;
    private static final String DEFAULT_HOST = "localhost";

    private final Logger LOGGER = LoggerFactory.getLogger(App.class);

    RestServerVerticle() {

    }

    // createHttpServer
    public void createHttpServer(Router router) {
        // Start server instance
        HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
                .setPort(DEFAULT_PORT)
                .setHost(DEFAULT_HOST));
        server.requestHandler(router).listen().onSuccess(r ->
                LOGGER.info("RestServerVerticle - Started listening on port {}", DEFAULT_PORT));
    }


    // responses 200, 500 etc




}
