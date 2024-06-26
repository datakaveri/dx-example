package com.example.common.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Query {
    private String sql;
    private JsonArray params;

    public Query(String sql, JsonArray params) {
        this.sql = sql;
        this.params = params;
    }

    public Query(JsonObject json) {
        this.sql = json.getString("query");
        this.params = json.getJsonArray("params", new JsonArray());
    }

    public String getSql() {
        return sql;
    }

    public JsonArray getParams() {
        return params;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("query", sql)
                .put("params", params);
    }
}
