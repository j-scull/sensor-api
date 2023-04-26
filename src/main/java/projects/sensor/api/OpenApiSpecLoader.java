package projects.sensor.api;

import io.vertx.core.Vertx;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class OpenApiSpecLoader {

    private final static String SPEC_FILE = "api.yaml";

    private Vertx vertx;

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public OpenApiSpecLoader() {
        vertx = Vertx.vertx();
    }

    public void loadSpec(){
        logger.info("OpenApiSpecLoader - Loading spec {}", SPEC_FILE);
        RouterBuilder.create(vertx, SPEC_FILE)
                .onSuccess(routerBuilder -> {
                    logger.info("OpenApiSpecLoader - Spec loaded successfully");
                    routerBuilder.operation("logData").handler(routingContext -> {
                        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                        logger.info("OpenApiSpecLoader - params = {}", params);
                        routingContext.response()
                                .setStatusCode(201);
                    });


                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    logger.error("OpenApiSpecLoader - Failed to load spec!");
                });
    }


}
