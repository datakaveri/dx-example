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

import io.vertx.core.VertxOptions;

import io.vertx.core.Vertx;

import io.vertx.spi.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

public class BookMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(BookMainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
            .host(System.getenv("INFINISPAN_HOST"))
            .port(Integer.parseInt(System.getenv("INFINISPAN_PORT")))
            .security()
            .authentication()
            .username(System.getenv("INFINISPAN_USER"))
            .password(System.getenv("INFINISPAN_PASS"));

        InfinispanClusterManager clusterManager = new InfinispanClusterManager(builder.build());

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

                BookService bookService = new BookDatabaseService(postgresService);
                BookController bookController = new BookController(bookService);

                JsonObject config = new JsonObject().put("port", config().getInteger("port", 8082));
        
                

                vertx.deployVerticle(bookController, new DeploymentOptions().setConfig(config), newres -> {
                    if (newres.succeeded()) {
                        LOGGER.info("BookController deployed successfully on port " + config.getInteger("port"));
                        startPromise.complete();
                        LOGGER.info(vertx.eventBus());
                    } else {
                        LOGGER.error("Failed to deploy BookController: " + newres.cause().getMessage());
                        startPromise.fail(res.cause());
                    }
                });
            } else {
                System.out.println("Failed to create clustered Vert.x instance: " + res.cause());
            }
        }); 

        // vertx.deployVerticle(bookController, new DeploymentOptions().setConfig(config), res -> {
        //     if (res.succeeded()) {
        //         LOGGER.info("BookController deployed successfully on port " + config.getInteger("port"));
        //         startPromise.complete();
        //         LOGGER.info(vertx.eventBus());
        //     } else {
        //         LOGGER.error("Failed to deploy BookController: " + res.cause().getMessage());
        //         startPromise.fail(res.cause());
        //     }
        // });
    }
}
