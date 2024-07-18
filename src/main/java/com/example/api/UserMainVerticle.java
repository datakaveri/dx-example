package com.example.api;

import com.example.postgres.PostgresVerticle;
import com.example.postgres.services.PostgresService;
import com.example.user.controller.UserController;
import com.example.user.services.UserDatabaseService;
import com.example.user.services.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(UserMainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting UserMainVerticle");
        LOGGER.info(vertx.eventBus());
        

        // Create proxy for PostgresService
        PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

        UserService userService = new UserDatabaseService(postgresService);

        UserController userController = new UserController(userService);
        JsonObject config = new JsonObject().put("port", config().getInteger("port", 8081));

        // Deploy UserController verticle
        vertx.deployVerticle(userController, new DeploymentOptions().setConfig(config), deploy -> {
            if (deploy.succeeded()) {
                LOGGER.info("UserController deployed successfully on port " + config.getInteger("port"));
                startPromise.complete();
            } else {
                LOGGER.error("Failed to deploy UserController: " + deploy.cause().getMessage());
                startPromise.fail(deploy.cause());
            }
        });
           
    }
}
