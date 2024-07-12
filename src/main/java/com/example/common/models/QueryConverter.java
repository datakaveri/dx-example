package com.example.common.models;

import io.vertx.core.json.JsonObject;

public class QueryConverter {

    public static void toJson(Query obj, JsonObject json) {
        json.put("sql", obj.getSql());
        if (obj.getParams() != null) {
            json.put("params", obj.getParams());
        }
    }

    public static void fromJson(JsonObject json, Query obj) {
        obj.setSql(json.getString("sql"));
        obj.setParams(json.getJsonArray("params"));
    }
}
