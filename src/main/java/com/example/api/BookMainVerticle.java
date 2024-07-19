package com.example.api;

import com.example.book.controller.BookController;
import com.example.book.services.BookDatabaseService;
import com.example.book.services.BookService;
import com.example.postgres.services.PostgresService;
import com.hazelcast.config.Config;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(BookMainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        Config hazelcastConfig = new Config();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();
                PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

                BookService bookService = new BookDatabaseService(postgresService);
                BookController bookController = new BookController(bookService);

                JsonObject config = new JsonObject().put("port", config().getInteger("port", 8082));
                vertx.deployVerticle(bookController, new DeploymentOptions().setConfig(config), handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("BookController deployed successfully on port " + config.getInteger("port"));
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to deploy BookController: " + handler.cause().getMessage());
                        startPromise.fail(handler.cause());
                    }
                });
            } else {
                LOGGER.error("Clustered Vert.x start failed: " + res.cause());
            }
        });
    }
}
