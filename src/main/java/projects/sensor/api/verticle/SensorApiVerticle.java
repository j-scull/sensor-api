package projects.sensor.api.verticle;

import projects.sensor.api.router.OpenApiRouter;

public class SensorApiVerticle extends RestServerVerticle {


    // Create router
    SensorApiVerticle() {
        OpenApiRouter openApiRouter = new OpenApiRouter();
    }

}
