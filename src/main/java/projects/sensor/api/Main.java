package projects.sensor.api;


import io.vertx.reactivex.core.Vertx;
import projects.sensor.api.verticle.RestServerVerticle;

public class Main {

    public static void main( String[] args ) {

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(RestServerVerticle.class.getName());
    }
}
