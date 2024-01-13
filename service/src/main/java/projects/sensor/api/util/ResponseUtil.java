package projects.sensor.api.util;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Responses for requests
 */
public class ResponseUtil {


    /**
     * 200 "OK" response
     * @param routingContext
     * @param jsonObject
     */
    public static void okResponse(RoutingContext routingContext, JsonObject jsonObject) {
        routingContext.response()
                .setStatusCode(200)
                .setStatusMessage("OK")
                .end(jsonObject.toBuffer());
    }

    /**
     * 201 "Created" response
     * @param routingContext
     */
    public static void createdResponse(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(201)
                .setStatusMessage("OK")
                .end();
    }

    /**
     * 400 "Bad Request" response
     * @param routingContext
     */
     public static void badRequestResponse(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(400)
                .setStatusMessage("Bad Request")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new JsonObject()
                        .put("code", 400)
                        .put("message", (routingContext.failure() != null) ? routingContext.failure().getMessage() : "Bad Request")
                        .encode());
    }

    /**
     * 404 "Not Found" response
     * @param routingContext
     */
    public static void notFoundResponse(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(404)
                .setStatusMessage("Not Found")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new JsonObject()
                        .put("code", 404)
                        .put("message", (routingContext.failure() != null) ? routingContext.failure().getMessage() : "Not Found")
                        .encode());
    }

    /**
     * 500 "Internal Server Error" response
     * @param routingContext
     */
    public static void internalServerError(RoutingContext routingContext) {
        routingContext.response()
                .setStatusCode(500)
                .setStatusMessage("Internal Server Error")
                .end();
    }
}
