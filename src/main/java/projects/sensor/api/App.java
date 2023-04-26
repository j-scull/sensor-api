package projects.sensor.api;



public class App {

    public static void main( String[] args ) {
        OpenApiSpecLoader specLoader = new OpenApiSpecLoader();
        specLoader.loadSpec();
    }
}
