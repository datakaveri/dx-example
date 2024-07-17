package com.example.api;

import com.example.postgres.services.PostgresService;
import com.example.book.controller.BookController;
import com.example.book.services.BookDatabaseService;
import com.example.book.services.BookService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(BookMainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

        BookService bookService = new BookDatabaseService(postgresService);
        BookController bookController = new BookController(bookService);

        JsonObject config = new JsonObject().put("port", config().getInteger("port", 8082));
        vertx.deployVerticle(bookController, new DeploymentOptions().setConfig(config), res -> {
            if (res.succeeded()) {
                LOGGER.info("BookController deployed successfully on port " + config.getInteger("port"));
                startPromise.complete();
            } else {
                LOGGER.error("Failed to deploy BookController: " + res.cause().getMessage());
                startPromise.fail(res.cause());
            }
        });
    }
}
