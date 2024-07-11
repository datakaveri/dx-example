package com.example.api;

import com.example.postgres.PostgresVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(MainVerticle.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start() {
        // Deploy PostgresVerticle
        vertx.deployVerticle(new PostgresVerticle(), res -> {
            if (res.succeeded()) {
                LOGGER.info("PostgresVerticle deployed successfully");
            } else {
                LOGGER.error("Failed to deploy PostgresVerticle: " + res.cause().getMessage());
            }
        });
    }

}
