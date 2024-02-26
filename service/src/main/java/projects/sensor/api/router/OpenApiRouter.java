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
import projects.sensor.api.config.Config;
import projects.sensor.api.service.SensorApiImpl;
import projects.sensor.api.util.ResponseUtil;

import java.io.File;

import static projects.sensor.api.util.FileUtil.copyFile;
import static projects.sensor.api.util.FileUtil.extractFilesToDirectory;
import static projects.sensor.api.util.FileUtil.replaceFile;

public class OpenApiRouter extends AbstractVerticle {

    private static final String SPEC_FILE = "api.yaml";
    private static final String SWAGGER_UI_DIR = "swagger-ui";

    private Vertx vertx;

    private Config config;
    private SensorApiImpl sensorApiImpl;

    private final Logger LOGGER = LoggerFactory.getLogger(OpenApiRouter.class);

    public OpenApiRouter(Vertx vertx, Config config, SensorApiImpl sensorApiImpl) {
        this.vertx = vertx;
        this.config = config;
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
                    // Setup Swagger-ui
                    if (config.isEnableSwaggerUi()) {
                        mountSwaggerUI(router).subscribe();
                    }
                    // Handle request for unknown paths
                    return router.errorHandler(404, ResponseUtil::notFoundResponse);
                })
                .onSuccess(r -> LOGGER.info("OpenApiRouter - Spec loaded successfully"))
                .onFailure(e -> LOGGER.error("OpenApiRouter - Failed to load spec! Exception"));
    }

    /**
     * Add a handler to the specified operation
     * @param routerBuilder - routerBuilder
     * @param operationId - the operationId defined in api.yaml
     * @param operationHandler - the handler for the opertion
     */
    private void addHandler(RouterBuilder routerBuilder, String operationId, Handler<RoutingContext> operationHandler) {
        LOGGER.info("Adding handler for operation = {}", operationId);
        routerBuilder.operation(operationId).handler(operationHandler).failureHandler(sensorApiImpl::failureHandler);
    }

    /**
     * Moves swagger-ui webjar files into a swagger-ui directory
     * Moves the api.yaml into this directory
     * Replaces swagger-initializer.js with a version that points to api.yaml
     * Creates the swagger-ui endpoint
     * @param router - a vertx router
     */
    private Single<File> mountSwaggerUI(Router router) {
        return extractFilesToDirectory(vertx.fileSystem(), "META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR)
                .map(r1 -> Single.concat(
                            replaceFile(vertx.fileSystem(), "swagger-initializer-override.js", SWAGGER_UI_DIR + "/swagger-initializer.js"),
                            copyFile(vertx.fileSystem(), SPEC_FILE, SWAGGER_UI_DIR + "/" + SPEC_FILE, true))
                        .toList()
                        .map(r2 ->  {
                                router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                                return new File(SWAGGER_UI_DIR);}))
                .flatMap(f -> f)
                .doOnError(e -> LOGGER.error("Failed to create swagger-ui endpoint, exception"))
                .doOnSuccess(s -> LOGGER.info("Created swagger-ui endpoint successfully"));
    }
}
