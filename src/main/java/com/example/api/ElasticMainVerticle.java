package com.example.api;

import com.example.elastic.ElasticsearchVerticle;
import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.vertx.core.Vertx.clusteredVertx;

public class ElasticMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ElasticMainVerticle.class);

    @Override
    public void start() {
        Config hazelcastConfig = new Config();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();
                vertx.deployVerticle(new ElasticsearchVerticle(), handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("ElasticsearchVerticle deployed successfully");
                    } else {
                        LOGGER.error("Failed to deploy ElasticsearchVerticle: " + handler.cause().getMessage());
                    }
                });
            } else {
                System.out.println("Clustered Vert.x start failed: " + res.cause());
            }
        });
    }
}
