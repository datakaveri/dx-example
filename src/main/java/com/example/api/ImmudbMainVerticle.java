package com.example.api;

import com.example.immudb.ImmudbVerticle;
import com.example.postgres.PostgresVerticle;
import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmudbMainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(ImmudbMainVerticle.class);

    @Override
    public void start() {
        Config hazelcastConfig = new Config();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

    Vertx.clusteredVertx(
        options,
        res -> {
          if (res.succeeded()) {
            vertx = res.result();
            vertx.deployVerticle(
                new ImmudbVerticle(),
                handler -> {
                  if (handler.succeeded()) {
                    LOGGER.info("Immudb deployed successfully");
                  } else {
                    LOGGER.error(
                        "Failed to deploy Immudb: " + handler.cause().getMessage());
                  }
                });
          } else {
            System.out.println("Clustered Vert.x start failed: " + res.cause());
          }
        });
    }

}
