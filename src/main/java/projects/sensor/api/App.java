package projects.sensor.api;


import projects.sensor.api.router.OpenApiRouter;

public class App {

    public static void main( String[] args ) {
        OpenApiRouter router = new OpenApiRouter();
        router.loadSpec();
    }
}
