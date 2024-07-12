package com.example.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import com.example.book.controller.BookController;
import com.example.book.services.BookDatabaseService;
import com.example.book.services.BookService;
import com.example.postgres.services.PostgresService;

public class BookMainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

        BookService bookService = new BookDatabaseService(postgresService);

        BookController bookController = new BookController(bookService);

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("port", 8082));
        vertx.deployVerticle(bookController, options);
    }
}
