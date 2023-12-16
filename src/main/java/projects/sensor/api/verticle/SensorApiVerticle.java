package projects.sensor.api.verticle;

import io.vertx.ext.web.RoutingContext;
import projects.sensor.api.databse.DatabaseClient;
import projects.sensor.api.router.OpenApiRouter;
import projects.sensor.api.service.SensorApiImpl;

public class SensorApiVerticle extends RestServerVerticle {



    @Override
    public void start() {

        // Todo - these fields should be read from config
        String databasePath = System.getProperty("user.dir") + "/target/db/test.db";
        String databaseUrl = "jdbc:sqlite:" + databasePath;
        String databaseDriverClass = "org.sqlite.JDBC";

        // Todo keep all database implementation within DataBaseClient
        DatabaseClient databaseClient = DatabaseClient.getInstance();
        databaseClient.connectToDatabase(vertx, databaseUrl, databaseDriverClass);

        SensorApiImpl sensorApiImpl = new SensorApiImpl(databaseClient);
        OpenApiRouter openApiRouter = new OpenApiRouter(vertx, sensorApiImpl);
        openApiRouter.buildRouterFromSpec().subscribe(router -> createHttpServer(router));
    }


}
