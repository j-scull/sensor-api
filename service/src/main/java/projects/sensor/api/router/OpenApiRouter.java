package projects.sensor.api.router;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.api.Main;
import projects.sensor.api.service.SensorApiImpl;
import projects.sensor.api.util.ResponseUtil;

import static projects.sensor.api.util.FileUtil.copyFile;
import static projects.sensor.api.util.FileUtil.extractFilesToDirectory;
import static projects.sensor.api.util.FileUtil.replaceFile;

public class OpenApiRouter extends AbstractVerticle {

    private static final String SPEC_FILE = "api.yaml";
    private static final String SWAGGER_UI_DIR = "swagger-ui";

    private final Vertx vertx;
    private final SensorApiImpl sensorApiImpl;
    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public OpenApiRouter(Vertx vertx, SensorApiImpl sensorApiImpl) {
        this.vertx = vertx;
        this.sensorApiImpl = sensorApiImpl;
    }

    /**
     * Builds a vertx router based on the operation defined in the spec file
     * Maps requests to handlers in SensorApiImpl
     * Adds error handler for unknown paths
     * Creates and mounts the swagger-ui endpoint
     * @return - Future<Router> - a handle to the built router
     */
    public Future<Router> buildRouterFromSpec() {
        LOGGER.info("OpenApiRouter - Loading spec {}", SPEC_FILE);
        return RouterBuilder.create(vertx.getDelegate(), SPEC_FILE)
                .map(routerBuilder -> {

//                    routerBuilder.securityHandler("ApiKeyAuth")
//                            .bindBlocking(config ->
//                                    APIKeyHandler.create(authProvider)
//                                            .header(config.getString("name")));

                    routerBuilder.setOptions(new RouterBuilderOptions()
                            .setOperationModelKey("operationModel"));

                    // Add handlers to the operations defined in the spec.
                    addHandler(routerBuilder, "logData", sensorApiImpl::logData);
                    addHandler(routerBuilder, "getDataForDate", sensorApiImpl::getDataForDate);
                    addHandler(routerBuilder, "getDataForDateRange", sensorApiImpl::getDataForDateRange);
                    addHandler(routerBuilder, "createSensor", sensorApiImpl::createSensor);
                    addHandler(routerBuilder, "listSensors", sensorApiImpl::listSensors);
                    addHandler(routerBuilder, "getSensor", sensorApiImpl::getSensor);

                    return routerBuilder;
                })
                .map(RouterBuilder::createRouter)
                .map(router -> {

                    // ToDo - revise sequence of events on start up
                    // Swagger-ui is mounted after server starts listening, should be happen before
                    mountSwaggerUI(router);

                    // Handle request for unknown paths
                    router.errorHandler(404, ResponseUtil::notFoundResponse);

                    return router;
                })
                .onSuccess(r -> LOGGER.info("OpenApiRouter - Spec loaded successfully"))
                .onFailure(e -> LOGGER.error("OpenApiRouter - Failed to load spec! Exception = {}", e.getCause()));
    }

    /**
     * Add a handler to the specified operation
     * @param routerBuilder - routerBuilder
     * @param operationId - the operationId defined in api.yaml
     * @param operationHandler - the handler for the opertion
     */
    private void addHandler(RouterBuilder routerBuilder, String operationId, Handler<RoutingContext> operationHandler) {
        LOGGER.info("Adding handler for operation = {}", operationId);
        routerBuilder.operation(operationId).handler(operationHandler).failureHandler(ResponseUtil::badRequestResponse);
    }

    /**
     * Moves swagger-ui webjar files into a swagger-ui directory
     * Moves the api.yaml into this directory
     * Replaces swagger-initializer.js with a version that points to api.yaml
     * Creates the swagger-ui endpoint
     * @param router - a vertx router
     */
    private void mountSwaggerUI(Router router) {
        extractFilesToDirectory(vertx.fileSystem(), "META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR)
                .doOnError(e -> LOGGER.error("Failed to create swagger-ui endpoint, exception = {}", e))
                .subscribe(r -> {
                    Single.concat(
                            replaceFile(vertx.fileSystem(), "swagger-initializer-override.js", SWAGGER_UI_DIR + "/swagger-initializer.js"),
                            copyFile(vertx.fileSystem(), SPEC_FILE, SWAGGER_UI_DIR + "/" + SPEC_FILE, true)
                    ).subscribe(res -> {
                        router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                        LOGGER.info("Created swagger-ui endpoint successfully");
                    }, e -> LOGGER.error("Failed to create swagger-ui endpoint, exception = {}", e));
                });
    }
}
