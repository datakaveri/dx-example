package com.example.api;

import com.example.IoT.controller.DeviceController;
import com.example.IoT.service.DeviceService;
import com.example.IoT.service.DeviceServiceImpl;
import com.example.elastic.service.ElasticsearchService;
import com.hazelcast.config.Config;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.vertx.core.Vertx.clusteredVertx;

public class DeviceMainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(DeviceMainVerticle.class);
    @Override
    public void start(Promise<Void> startPromise) {
        Config hazelcastConfig = new Config();
        HazelcastClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
        clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();
                ElasticsearchService elasticsearchService = ElasticsearchService.createProxy(vertx, "elastic.service");

                DeviceService deviceService = new DeviceServiceImpl(elasticsearchService);
                DeviceController deviceController = new DeviceController(deviceService);

                JsonObject config = new JsonObject().put("port", config().getInteger("port", 8083));
                vertx.deployVerticle(deviceController, new DeploymentOptions().setConfig(config), handler -> {
                    if (handler.succeeded()) {
                        LOGGER.info("DeviceController deployed successfully on port " + config.getInteger("port"));
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to deploy DeviceController: " + handler.cause().getMessage());
                        startPromise.fail(handler.cause());
                    }
                });
            } else {
                LOGGER.error("Clustered Vert.x start failed: " + res.cause());
            }
        });
    }
}
