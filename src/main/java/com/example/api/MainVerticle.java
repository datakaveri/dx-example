package com.example.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import com.example.user.controller.UserController;
import com.example.book.controller.BookController;
import com.example.postgres.PostgresVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.deployVerticle(new PostgresVerticle());

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8081));
        vertx.deployVerticle(new UserController(), options);

        options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8082));
        vertx.deployVerticle(new BookController(), options);
    }
}
