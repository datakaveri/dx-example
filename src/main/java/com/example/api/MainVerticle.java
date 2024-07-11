package com.example.api;

import com.example.book.controller.BookController;
import com.example.book.services.BookDatabaseService;
import com.example.book.services.BookService;
import com.example.postgres.PostgresVerticle;
import com.example.postgres.services.PostgresService;
import com.example.user.controller.UserController;
import com.example.user.services.UserDatabaseService;
import com.example.user.services.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

                // Create PostgresService proxy
                PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

                // Initialize and deploy controllers
                deployControllers(postgresService);
            } else {
                LOGGER.error("Failed to deploy PostgresVerticle: " + res.cause().getMessage());
            }
        });
    }

    private void deployControllers(PostgresService postgresService) {
        // Create and deploy UserController
        UserService userService = new UserDatabaseService(postgresService);
        UserController userController = new UserController(userService);
        deployVerticle(userController, 8081, "UserController");

        // Create and deploy BookController
        BookService bookService = new BookDatabaseService(postgresService);
        BookController bookController = new BookController(bookService);
        deployVerticle(bookController, 8082, "BookController");
    }

    private void deployVerticle(AbstractVerticle verticle, int port, String verticleName) {
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("port", port));
        vertx.deployVerticle(verticle, options, res -> {
            if (res.succeeded()) {
                LOGGER.info(verticleName + " deployed successfully on port " + port);
            } else {
                LOGGER.error("Failed to deploy " + verticleName + ": " + res.cause().getMessage());
            }
        });
    }
}
