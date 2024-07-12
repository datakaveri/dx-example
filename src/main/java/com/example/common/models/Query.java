package com.example.common.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@DataObject
public class Query {

    private String sql;
    private JsonArray params;

    public Query(String sql, JsonArray params) {
        this.sql = sql;
        this.params = params;
    }

    public Query() {
        // Required by @DataObject
    }

    public String getSql() {
        return sql;
    }

    public Query setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public JsonArray getParams() {
        return params;
    }

    public Query setParams(JsonArray params) {
        this.params = params;
        return this;
    }

    // Serializer method
    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("sql", this.sql);
        if (this.params != null) {
            json.put("params", this.params);
        }
        return json;
    }

    // Deserializer method
    public static Query fromJson(JsonObject json) {
        return new Query(json.getString("sql"), json.getJsonArray("params"));
    }

    // Converter method for @DataObject
    public Query(JsonObject json) {
        QueryConverter.fromJson(json, this);
    }

    public JsonObject toJsonDataObject() {
        JsonObject json = new JsonObject();
        QueryConverter.toJson(this, json);
        return json;
    }
}
