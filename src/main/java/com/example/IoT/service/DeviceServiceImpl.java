package com.example.IoT.service;

import com.example.IoT.models.Device;
import com.example.elastic.service.ElasticsearchService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DeviceServiceImpl implements DeviceService {
    private final static Logger LOGGER = LogManager.getLogger(DeviceServiceImpl.class);
    private final ElasticsearchService elasticsearchService;

    public DeviceServiceImpl(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public Future<Device> add(Device device) {
        Promise<Device> promise = Promise.promise();
        JsonObject json = JsonObject.mapFrom(device);
        elasticsearchService.index("devices", device.getDeviceId(), json).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(device);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    @Override
    public Future<Device> update(Device device) {
        Promise<Device> promise = Promise.promise();
        JsonObject json = JsonObject.mapFrom(device);
        elasticsearchService.update("devices", device.getDeviceId(), json).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(device);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    @Override
    public Future<JsonObject> delete(String deviceId) {
        Promise<JsonObject> promise = Promise.promise();
        elasticsearchService.delete("devices", deviceId).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("id", deviceId));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    @Override
    public Future<Device> getById(String deviceId) {
        Promise<Device> promise = Promise.promise();

        elasticsearchService.search("devices", deviceId).onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("res: "+ ar.result());
                JsonObject json = ar.result();
                Device device = json.mapTo(Device.class);
                promise.complete(device);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }
    @Override
    public Future<List<Device>> getAllDevices() {
        Promise<List<Device>> promise = Promise.promise();

        elasticsearchService.fetchAll("devices").onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject json = ar.result();
                JsonArray response = json.getJsonArray("response");
                List<Device> devices = new ArrayList<>();
                response.forEach(hit -> {
                    Device device = ((JsonObject) hit).mapTo(Device.class);
                    devices.add(device);
                });
                promise.complete(devices);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

}
