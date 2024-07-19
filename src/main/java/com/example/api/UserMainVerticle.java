package com.example.api;

import com.example.postgres.services.PostgresService;
import com.example.user.controller.UserController;
import com.example.user.services.UserDatabaseService;
import com.example.user.services.UserService;
import com.hazelcast.config.Config;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(UserMainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) {
        Config hazelcastConfig = new Config();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();
                PostgresService postgresService = PostgresService.createProxy(vertx, "postgres.service");

                UserService userService = new UserDatabaseService(postgresService);
                UserController userController = new UserController(userService);

                JsonObject config = new JsonObject().put("port", config().getInteger("port", 8081));
                vertx.deployVerticle(userController, new DeploymentOptions().setConfig(config), handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("UserController deployed successfully on port " + config.getInteger("port"));
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to deploy UserController: " + handler.cause().getMessage());
                        startPromise.fail(handler.cause());
                    }
                });
            } else {
                LOGGER.error("Clustered Vert.x start failed: " + res.cause());
            }
        });
    }
}
