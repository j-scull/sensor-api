package projects.sensor.api;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.file.FileSystem;



//import jdk.jpackage.internal.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.model.DataPoint;
import projects.sensor.model.Sensor;

import java.io.File;
import java.util.Date;

public class OpenApiSpecLoader {

    private static final String SPEC_FILE = "api.yaml";
    private final String SWAGGER_UI_DIR = "swagger-ui";
    private static final int PORT = 9090;

//    private Vertx vertx;
    private Vertx vertx;

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public OpenApiSpecLoader() {
        vertx = Vertx.vertx();
    }

    public void loadSpec() {
        logger.info("OpenApiSpecLoader - Loading spec {}", SPEC_FILE);
        RouterBuilder.create(vertx.getDelegate(), SPEC_FILE)
                .onSuccess(routerBuilder -> {


//                    routerBuilder.securityHandler("ApiKeyAuth")
//                            .bindBlocking(config ->
//                                    APIKeyHandler.create(authProvider)
//                                            .header(config.getString("name")));

                    RouterBuilderOptions options = new RouterBuilderOptions();
                    options.setOperationModelKey("operationModel");
                    routerBuilder.setOptions(options);

                    routerBuilder.operation("logData")
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter temperature = params.queryParameter("temperature");
                                RequestParameter humidity = params.queryParameter("humidity");
                                Date dateTime = new Date();
                                logger.info("logData - temperature = {}, humidity = {}, time = {}", temperature, humidity, dateTime);
                                routingContext.response()
                                        .setStatusCode(201)
                                        .setStatusMessage("OK")
                                        .end();
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.info("OpenApiSpecLoader - {} error", operation.getString("operationId"));
                            });

                    routerBuilder.operation("listDataPoints")
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter year = params.queryParameter("year");
                                RequestParameter month = params.queryParameter("month");
                                RequestParameter date = params.queryParameter("date");
                                logger.info("listDataPoints - sensorId = {}, year = {}, month = {}, date = {}", sensorId, year, month, date);
                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(getDataPoints().toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.info("OpenApiSpecLoader - {} error", operation.getString("operationId"));
                            });

                    routerBuilder.operation("dataPointsRange")
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter start = params.queryParameter("start");
                                RequestParameter stop = params.queryParameter("stop");
                                logger.info("listDataPoints - sensorId = {}, start = {}, stop = {}", sensorId, start, stop);

                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(getDataPoints().toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.info("OpenApiSpecLoader - {} error", operation.getString("operationId"));
                            });

                    routerBuilder.operation("listSensors")
                            .handler(routingContext -> {
                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                logger.info("listSensors - params = {}", params);
                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(getSensors().toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.info("OpenApiSpecLoader - {} error", operation.getString("operationId"));
                            });

                    // Generate the router
                    Router router = routerBuilder.createRouter();

                    // Create swagger-ui end point
                    loadWebJars("META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR, handler -> {
                                replaceFile("swagger-initializer-override.js", SWAGGER_UI_DIR + "/swagger-initializer.js");
                                copyFile(SPEC_FILE, SWAGGER_UI_DIR + "/" + SPEC_FILE);
                                router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                    }).subscribe(res -> logger.info("Created swagger-ui endpoint successfully"),
                            e -> logger.error("Failed to create swagger-ui endpoint, exception = {}", e));

                    router.errorHandler(404, routingContext -> {
                        JsonObject errorObject = new JsonObject();
                        errorObject.put("code", 404);
                        errorObject.put("message",
                                (routingContext.failure() != null) ? routingContext.failure().getMessage() : "Not Found"
                        );
                        routingContext.response()
                                .setStatusCode(404)
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .end(errorObject.encode());
                    });

                    // Start server instance
                    HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
                            .setPort(PORT)
                            .setHost("localhost"));
                    server.requestHandler(router).listen().onSuccess(r ->
                            logger.info("OpenApiSpecLoader - Started listening on port {}", PORT));

                    logger.info("OpenApiSpecLoader - Spec loaded successfully");

                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    logger.error("OpenApiSpecLoader - Failed to load spec!");
                });
    }

    // Mock a response
    private JsonObject getDataPoints() {
        int n = 4;
        DataPoint[] dataPoints = new DataPoint[n];//
        for (int i = 0; i < n; i++) {
            int temperature = (int) (Math.random() * 30);
            int humidity = (int) (Math.random() * 100);
            dataPoints[i] = new DataPoint(temperature, humidity, String.valueOf(i));
        }
        return new JsonObject().put("data", dataPoints);
    }

    // Mock a reponse
    private JsonObject getSensors() {
        Sensor[] sensors = new Sensor[4];
        sensors[0] = new Sensor("1", "somewhere", "01-01-2023");
        sensors[1] = new Sensor("2", "somewhere", "01-01-2023");
        sensors[2] = new Sensor("3", "somewhere else", "01-01-2023");
        sensors[3] = new Sensor("4", "somewhere slse", "01-01-2023");
        return new JsonObject().put("data", sensors);
    }

//    private Single<Future<Void>> loadWebJar(String source, String dest) {
//        return Single.defer(() -> Single.just(vertx.fileSystem().mkdir(dest).onSuccess(r -> {
//            vertx.fileSystem().exists(source, exists -> {
//                if (exists.result()) {
//                    try {
//                        Single.just(vertx.fileSystem().copyRecursive(source, dest, true, res -> {
//                            logger.info("Successfully copied source = {} to destination = {}", source, dest);
//                        }));
//                    } catch (Exception e) {
//                        logger.error("Unable to copy source = {} to destination = {}, excetion = {}", source, dest, e);
//                    }
//                } else {
//                    logger.error("Could not find source webjar location = {}", source);
//                }
//            });
//        }).onFailure(e -> logger.error("Unable to create destination directory = {}", dest))));
//    }


    private Single<FileSystem> loadWebJars(String source, String dest, Handler<AsyncResult<Void>> handler) {
        return Single.defer(() -> Single.just(vertx.fileSystem().mkdir(dest))
                .doOnSuccess(r -> {
                    logger.info("Created directory = {}", dest);
                    Single<Boolean> b = vertx.fileSystem().rxExists(source);
                    vertx.fileSystem().rxExists(source)
                            .subscribe(exists -> {
                                if (exists) {
                                    logger.info("Copying from directory {}", source);
                                    vertx.fileSystem().rxCopyRecursive(source, dest, true)
                                            .andThen(Single.just(true))
                                            .subscribe(s -> {
                                                logger.info("Copied files from {} to destination = {}", source, dest);
                                                if (handler != null) {
                                                    handler.handle(null);
                                                }
                                            }, e -> logger.error("Failed to copy files from {} to {}, reason = {}", source, dest, e));
                                } else {
                                    logger.error("Source directory {} does not exist", source);
                                }
                            });
                }));
    }


    private Single<Object> loadWebJars2(String source, String dest, Handler<AsyncResult<Void>> handler) {
        return Single.defer(() -> Single.just(vertx.fileSystem().rxMkdir(dest))
                .map(r -> {
                    logger.info("Created directory = {}", dest);
                    return vertx.fileSystem().rxExists(source)
                            .map(exists -> {
                                if (exists) {
                                    logger.info("Copying from directory {}", source);
                                    return Single.just(vertx.fileSystem().rxCopyRecursive(source, dest, true));
                                } else {
                                    logger.error("Source directory {} does not exist", source);
                                    return Single.just(dest);
                                }
                            });
                }));
    }

    private void copyFile(String sourceFilePath, String destFilePath) {
        vertx.fileSystem().copy(sourceFilePath, destFilePath, r -> {
            if (r.succeeded()) {
                logger.info("Copied {} to {}", sourceFilePath, destFilePath);
            } else if (r.failed()) {
                logger.error("Failed to copy {} to {}", sourceFilePath, destFilePath);
            }
        });
    }

    private void replaceFile(String sourceFilePath, String toReplaceFilePath) {
        vertx.fileSystem().delete(toReplaceFilePath, r -> {
            if (r.succeeded()) {
                logger.info("Removed {}", toReplaceFilePath);
                copyFile(sourceFilePath, toReplaceFilePath);
            } else if (r.failed()) {
                logger.error("Failed to remove {}", toReplaceFilePath);
            }
        });
    }

    private Single<File> copyFileRx(String sourceFilePath, String destFilePath) {
        return vertx.fileSystem().rxCopy(sourceFilePath, destFilePath)
                .andThen(Single.just(new File(destFilePath)));
    }

    private Single<File> replaceFileRx(String sourceFilePath, String destFilePath) {
        return vertx.fileSystem().rxDelete(destFilePath)
                .andThen(copyFileRx(sourceFilePath, destFilePath));
    }


}
