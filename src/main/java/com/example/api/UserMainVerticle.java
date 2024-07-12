package com.example.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import com.example.user.controller.UserController;
import com.example.user.services.UserDatabaseService;
import com.example.user.services.UserService;
import com.example.postgres.services.PostgresService;

public class UserMainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

        UserService userService = new UserDatabaseService(postgresService);

        UserController userController = new UserController(userService);
        
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8081));
        vertx.deployVerticle(userController, options);
    }
}
