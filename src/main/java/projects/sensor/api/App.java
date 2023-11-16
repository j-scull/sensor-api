package projects.sensor.api;



public class App {

    public static void main( String[] args ) {
        OpenApiRouter router = new OpenApiRouter();
        router.loadSpec();
    }
}
