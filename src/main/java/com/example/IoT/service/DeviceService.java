package com.example.IoT.service;

import com.example.IoT.models.Device;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;


public interface DeviceService {
    Future<Device> add(Device device);
    Future<Device> update(Device device);
    Future<JsonObject> delete(String deviceId);
    Future<Device> getById(String deviceId);
    Future<List<Device>> getAllDevices();
}
