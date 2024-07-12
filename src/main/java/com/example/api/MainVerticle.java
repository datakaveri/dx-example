package com.example.api;
import com.example.postgres.PostgresVerticle;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new PostgresVerticle());
    }
}
