package com.example.IoT.controller;

import com.example.IoT.models.Device;
import com.example.IoT.service.DeviceService;
import com.example.common.models.response.FailureResponseHandler;
import com.example.common.models.response.ResponseType;
import com.example.common.models.response.SuccessResponseHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DeviceController extends AbstractVerticle {
    private final static Logger LOGGER = LogManager.getLogger(DeviceController.class);
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/devices/:id").handler(this::getDeviceById);
        router.get("/devices").handler(this::getAllDevices);
        router.post("/devices").handler(this::addDevice);
        router.put("/devices/:id").handler(this::updateDevice);
        router.delete("/devices/:id").handler(this::deleteDevice);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8083, res -> {
                    if (res.succeeded()) {
                        LOGGER.info("HTTP server started on port " + config().getInteger("port", 8083));
                    } else {
                        LOGGER.error("Failed to start HTTP server: " + res.cause().getMessage());
                    }
                });
    }

    private void getDeviceById(RoutingContext context) {
        HttpServerResponse response = context.response();
        String deviceId = context.request().getParam("id");
        deviceService.getById(deviceId).onComplete(ar -> {
            if (ar.succeeded()) {
                Device device = ar.result();
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(device));
            } else {
                LOGGER.error("Failed to get device: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void addDevice(RoutingContext context) {
        HttpServerResponse response = context.response();
        if (context.getBodyAsJson() == null) {
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }
        try {
            Device device = context.getBodyAsJson().mapTo(Device.class);
            deviceService.add(device).onComplete(ar -> {
                if (ar.succeeded()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Created.getCode(), JsonObject.mapFrom(ar.result()));
                } else {
                    LOGGER.error("Failed to add device: " + ar.cause().getMessage());
                    FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to parse request body: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, "Invalid JSON body");
        }
    }

    private void updateDevice(RoutingContext context) {
        HttpServerResponse response = context.response();
        if (context.getBodyAsJson() == null) {
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }
        String deviceId = context.request().getParam("id");
        try {
            Device device = context.getBodyAsJson().mapTo(Device.class);
            device.setDeviceId(deviceId);

            deviceService.update(device).onComplete(ar -> {
                if (ar.succeeded()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(ar.result()));
                } else {
                    LOGGER.error("Failed to update device: " + ar.cause().getMessage());
                    FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to parse request body: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, "Invalid JSON body");
        }
    }

    private void deleteDevice(RoutingContext context) {
        HttpServerResponse response = context.response();
        String deviceId = context.request().getParam("id");
        deviceService.delete(deviceId).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), ar.result());
            } else {
                LOGGER.error("Failed to delete device: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }
    private void getAllDevices(RoutingContext context) {
        HttpServerResponse response = context.response();
        deviceService.getAllDevices().onComplete(ar -> {
            if (ar.succeeded()) {
                List<Device> devices = ar.result();
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), new JsonObject().put("devices", devices));
            } else {
                LOGGER.error("Failed to get device: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }
}
