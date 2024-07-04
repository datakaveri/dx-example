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

    public String getSql() {
        return sql;
    }

    public JsonArray getParams() {
        return params;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("sql", this.sql);
        if (this.params != null) {
            json.put("params", this.params);
        }
        return json;
    }
}
