package projects.sensor.api;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

//import jdk.jpackage.internal.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.model.DataPoint;
import projects.sensor.model.Sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;


public class OpenApiSpecLoader {

    private static final String SPEC_FILE = "api.yaml";

    // ToDo - currently writing to swagger-ui in the main project dir, needs a location in the target dir
    private final String SWAGGER_UI_DIR = "swagger-ui";
    private static final int PORT = 9090;

    private Vertx vertx;

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public OpenApiSpecLoader() {
        vertx = Vertx.vertx();
    }

    public void loadSpec() {
        logger.info("OpenApiSpecLoader - Loading spec {}", SPEC_FILE);
        RouterBuilder.create(vertx, SPEC_FILE)
                .onSuccess(routerBuilder -> {

                    logger.info("OpenApiSpecLoader - Spec loaded successfully");

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
                    loadWebJar("META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR).subscribe(r -> {
                        // ToDo - use rx patterns here
                        // delete existing swagger-ui file
                        // on success, copy new wagger-initializer-override.js and api.yaml to swagger-ui directory
                        // merge completable output, on success mount swagger-ui to endpoint
                        copyFile("swagger-initializer-override.js", "swagger-ui/swagger-initializer.js");
                        copyFile("api.yaml", "swagger-ui/api.yaml");
                        router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                        logger.info("Created swagger-ui endpoint successfully");
                    }, e -> logger.error("Failed to create swagger-ui endpoint"));

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
                    HttpServer server = vertx.createHttpServer(new HttpServerOptions()
                            .setPort(PORT)
                            .setHost("localhost"));
                    server.requestHandler(router).listen().onSuccess(r ->
                            logger.info("OpenApiSpecLoader - Started listening on port {}", PORT));

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

    private Single<Future<Void>> loadWebJar(String source, String dest) {
        return Single.defer(() -> Single.just(vertx.fileSystem().mkdir(dest).onSuccess(r -> {
            vertx.fileSystem().exists(source, exists -> {
                if (exists.result()) {
                    try {
                        vertx.fileSystem().copyRecursive(source, dest, true, res -> {
                            logger.info("Successfully copied source = {} to destination = {}", source, dest);
                        });
                    } catch (Exception e) {
                        logger.error("Unable to copy source = {} to destination = {}, excetion = {}", source, dest, e);
                    }
                } else {
                    logger.error("Could not find source webjar location = {}", source);
                }
            });
        }).onFailure(e -> logger.error("Unable to create destination directory = {}", dest))));
    }

    // ToDo - handle this asynchronously
    private void copyFile(String sourceFilePath, String destFilePath) throws IOException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(sourceFilePath);
        Path path = Paths.get(url.getPath());
        byte[] buffer = Files.readAllBytes(path);

        File targetFile = new File(destFilePath);
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();
    }


//    private Single<Void> deleteFile(String File) {
//
//    }


}
