package com.example.common.models;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class QueryResult {
    private JsonArray rows;
    private String error;

    public QueryResult(JsonArray rows) {
        this.rows = rows;
        this.error = null;
    }

    public QueryResult(String error) {
        this.rows = new JsonArray();
        this.error = error;
    }

    public JsonArray getRows() {
        return rows;
    }

    public String getError() {
        return error;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("rows", rows);
        if (error != null) {
            json.put("error", error);
        }
        return json;
    }
}
