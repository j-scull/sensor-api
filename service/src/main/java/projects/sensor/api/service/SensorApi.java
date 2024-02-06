package projects.sensor.api.service;

import io.vertx.ext.web.RoutingContext;

// Why not enable and use the generated APIs?
public interface SensorApi {

    /**
     *
     * @param routingContext
     */
    void logData(RoutingContext routingContext);

    /**
     *
     * @param routingContext
     */
    void getDataForDate(RoutingContext routingContext);

    /**
     *
     * @param routingContext
     */
    void getDataForDateRange(RoutingContext routingContext);

    /**
     *
     * @param routingContext
     */
    void createSensor(RoutingContext routingContext);

    /**
     *
     * @param routingContext
     */
    void listSensors(RoutingContext routingContext);

    /**
     *
     * @param routingContext
     */
    void getSensor(RoutingContext routingContext);

}
