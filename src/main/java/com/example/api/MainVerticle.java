package com.example.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import com.example.user.controller.UserController;
import com.example.user.services.UserDatabaseService;
import com.example.user.services.UserService;
import com.example.book.controller.BookController;
import com.example.book.services.BookDatabaseService;
import com.example.book.services.BookService;
import com.example.postgres.PostgresVerticle;
import com.example.postgres.services.PostgresService;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {

        PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

        // Create BookDatabaseService using PostgresService proxy
        BookService bookService = new BookDatabaseService(postgresService);

        // Deploy BookController verticle
        BookController bookController = new BookController(bookService);

        UserService userkService = new UserDatabaseService(postgresService);

        // Deploy BookController verticle
        UserController userController = new UserController(userkService);
        
        vertx.deployVerticle(new PostgresVerticle());

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8081));
        vertx.deployVerticle(userController, options);

        options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8082));
        vertx.deployVerticle(bookController, options);
    }
}
