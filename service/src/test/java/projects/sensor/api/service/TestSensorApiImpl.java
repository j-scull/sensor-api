package projects.sensor.api.service;

import io.reactivex.Single;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import projects.sensor.api.database.DatabaseClient;
import projects.sensor.api.service.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

@RunWith(EasyMockRunner.class)
public class TestSensorApiImpl {

    @Mock
    private DatabaseClient dataBaseClient;
    @Mock
    private RoutingContext routingContext;
    @Mock
    private HttpServerResponse httpServerResponse;

    @TestSubject
    private SensorApiImpl sensorApiImpl = new SensorApiImpl(dataBaseClient);

    @Test
    public void testLogData_Success() {

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("temperature", "9");
        jsonBody.put("humidity", "87");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.insertData(anyObject())).andReturn(Single.just(new UpdateResult(1, new JsonArray())));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(201)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.logData(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void testLogData_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("temperature", "9");
        jsonBody.put("humidity", "87");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return an error
        expect(dataBaseClient.insertData(anyObject())).andReturn(Single.error(new Exception("Error!")));

        // Set up the Internal Server Error response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.logData(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDate_Success_ReturnsData() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap .put("dateTime", new RequestParameterImpl("2024-01-25T18:29:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "temperature", "humidity", "time");
        List<JsonArray> resultsList = new ArrayList<>();
        resultsList.add(createGetDataResults("123", "11", "90", "2024-01-25T20:00:00.000Z"));
        resultsList.add(createGetDataResults("123", "11", "90", "2024-01-25T20:01:00.000Z"));
        resultsList.add(createGetDataResults("123", "10", "90", "2024-01-25T20:02:00.000Z"));
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);

    }

    @Test
    public void getDataForDate_Success_ReturnsEmpty() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap .put("dateTime", new RequestParameterImpl("2024-01-19T18:29:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Database query result - empty
        List<String> fieldNames = Arrays.asList("sensorId", "temperature", "humidity", "time");
        List<JsonArray> resultsList = new ArrayList<>();
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);

    }


    @Test
    public void getDataForDate_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap .put("dateTime", new RequestParameterImpl("2024-01-19T18:29:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database connection to fail
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.error(new Exception("Error!")));

        // Set up the Internal Server Error response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDateRange_Success() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request paramaters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap.put("from", new RequestParameterImpl("2024-01-25T20:00:00.000Z"));
        queryParameterMap.put("until", new RequestParameterImpl("2024-01-25T21:00:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "temperature", "humidity", "time");
        List<JsonArray> resultsList = new ArrayList<>();
        resultsList.add(createGetDataResults("123", "11", "90", "2024-01-25T20:00:00.000Z"));
        resultsList.add(createGetDataResults("123", "11", "90", "2024-01-25T20:01:00.000Z"));
        resultsList.add(createGetDataResults("123", "10", "90", "2024-01-25T20:02:00.000Z"));
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDateRange(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDateRange_Success_EmptyResponse() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request paramaters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap.put("from", new RequestParameterImpl("2024-01-23T19:00:00.000Z"));
        queryParameterMap.put("until", new RequestParameterImpl("2024-01-23T20:00:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "temperature", "humidity", "time");
        List<JsonArray> resultsList = new ArrayList<>();
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful but empty
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDateRange(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDateRange_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request paramaters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap.put("from", new RequestParameterImpl("2024-01-23T19:00:00.000Z"));
        queryParameterMap.put("until", new RequestParameterImpl("2024-01-23T20:00:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database connection to fail
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.error(new Exception("Error!")));
        // Set up the Internal Server Error response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDateRange(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDateRange_InvalidTimeParameters() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request paramaters - invalid as 'from' is after 'until'
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        Map<String, RequestParameter> queryParameterMap = new HashMap<>();
        queryParameterMap.put("from", new RequestParameterImpl("2024-01-23T20:00:00.000Z"));
        queryParameterMap.put("until", new RequestParameterImpl("2024-01-23T19:00:00.000Z"));
        RequestParametersImpl requestParameters = createPathAndQueryParameters(pathParameterMap, queryParameterMap);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        routingContext.fail(isA(IllegalArgumentException.class));
        expectLastCall();
        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDateRange(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void createSensor_Success() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("location", "soemwhere");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.insertSensor(anyObject())).andReturn(Single.just(new UpdateResult(1, new JsonArray())));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(201)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.createSensor(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }


    @Test
    public void createSensor_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("location", "soemwhere");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.insertSensor(anyObject())).andReturn(Single.error(new Exception("Error!")));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.createSensor(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void listSensors_Success() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "location", "creationTime");
        List<JsonArray> resultsList = new ArrayList<>();
        resultsList.add(createGetSensorResults("123", "somewhere", "2024-01-24T21:00:00.000Z"));
        resultsList.add(createGetSensorResults("123", "somewhere", "2024-01-24T21:00:01.000Z"));
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up database client to return successful response with data
        expect(dataBaseClient.selectAllSensors()).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.listSensors(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void listSensors_Success_EmptyResponse() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "location", "creationTime");
        List<JsonArray> resultsList = new ArrayList<>();
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data - empty
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up database client to return an empty response
        expect(dataBaseClient.selectAllSensors()).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.listSensors(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void listSensors_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Set up database client to return an error
        expect(dataBaseClient.selectAllSensors()).andReturn(Single.error(new Exception("Error!")));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.listSensors(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getSensor_Success() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        RequestParametersImpl requestParameters = createPathParameters(pathParameterMap);
        // Database query result
        List<String> fieldNames = Arrays.asList("sensorId", "location", "creationTime");
        List<JsonArray> resultsList = new ArrayList<>();
        resultsList.add(createGetSensorResults("123", "somewhere", "2024-01-24T21:00:01.000Z"));
        ResultSet resultSet = createResultSet(fieldNames, resultsList);
        // Response data
        JsonObject responseData = createResponseData(fieldNames, resultsList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectSensor(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(responseData.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getSensor(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getSensor_Success_EmptyResponse() {
        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        RequestParametersImpl requestParameters = createPathParameters(pathParameterMap);

        // Database query result - no matches
        List<String> fieldNames = Arrays.asList("sensorId", "location", "creationTime");
        ResultSet resultSet = createResultSet(fieldNames, new ArrayList<>());

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectSensor(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        routingContext.fail(isA(NotFoundException.class));
        expectLastCall();

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getSensor(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getSensor_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request parameters
        Map<String, RequestParameter> pathParameterMap = new HashMap<>();
        pathParameterMap .put("sensorId", new RequestParameterImpl("123"));
        RequestParametersImpl requestParameters = createPathParameters(pathParameterMap);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectSensor(anyObject())).andReturn(Single.error(new Exception("Error!")));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getSensor(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    private RequestParametersImpl createPathParameters(Map<String, RequestParameter> parameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setPathParameters(parameters);
        return requestParameters;
    }

    private RequestParametersImpl createPathAndQueryParameters(Map<String, RequestParameter> pathParameters, Map<String, RequestParameter> queryParameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setPathParameters(pathParameters);
        requestParameters.setQueryParameters(queryParameters);
        return requestParameters;
    }

    private RequestParameters createRequestBody(JsonObject jsonObject) {
        RequestParameterImpl requestParameter = new RequestParameterImpl(jsonObject);
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setBody(requestParameter);
        return requestParameters;
    }

    private JsonArray createGetDataResults(String sensorId, String temperature, String humidity, String dateTime) {
        return new JsonArray()
                .add(sensorId)
                .add(temperature)
                .add(humidity)
                .add(dateTime);
    }

    private JsonArray createGetSensorResults(String sensorId, String location, String creationTime) {
        return new JsonArray()
                .add(sensorId)
                .add(location)
                .add(creationTime);
    }

    private ResultSet createResultSet(List<String> fieldNames, List<JsonArray> resultsList) {
        return new ResultSet()
                .setColumnNames(fieldNames)
                .setResults(resultsList);
    }


    private JsonObject createResponseData(List<String> fieldNames, List<JsonArray> resultsList) {
        List<JsonObject> responseList = new ArrayList<>();
        // Allow for either empty results, otherwise each results size must match fieldNames size
        if (!resultsList.isEmpty()) {
            resultsList.forEach(results -> {
                if (fieldNames.size() != results.size()) {
                    throw new RuntimeException("fieldNames =  " + fieldNames + " and results = " + results + " are different sizes!");
                }
                JsonObject jsonObject = new JsonObject();
                for (int i = 0; i < fieldNames.size(); i++) {
                    jsonObject.put(fieldNames.get(i), results.getValue(i));
                }
                responseList.add(jsonObject);
            });
        }
        return new JsonObject().put("data", responseList);
    }
}
