package projects.sensor.api.service;

import io.vertx.ext.web.RoutingContext;

public interface SensorApi {
    public void logData(RoutingContext routingContext);
    public void getDataForDate(RoutingContext routingContext);
    public void getDataForDateRange(RoutingContext routingContext);
    public void createSensor(RoutingContext routingContext);
    public void listSensors(RoutingContext routingContext);
    public void getSensor(RoutingContext routingContext);
//
//    // Generated API methods
//    Future<ApiResponse<Void>> logData(UpdateRequest updateRequest);
//    Future<ApiResponse<List<GetDataResponse>>> getDataForDate(String sensorId, QueryDateTime queryDateTime);
//    Future<ApiResponse<List<GetDataResponse>>> getDataForDateRange(String sensorId, GetDataForDateRangeRequest getDataForDateRangeRequest);
//    Future<ApiResponse<Void>> createSensor(CreateSensorRequest createSensorRequest);
//    Future<ApiResponse<GetSensorResponse>> getSensor(String sensorId);
//    Future<ApiResponse<List<GetSensorResponse>>> listSensors();


}
