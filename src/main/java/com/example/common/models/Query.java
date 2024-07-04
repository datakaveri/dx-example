package com.example.common.models;

import io.vertx.core.shareddata.Shareable;
import io.vertx.core.json.JsonObject;

public class Query implements Shareable {

    private String sql;
    private JsonObject params;

    public Query(String sql, JsonObject params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public JsonObject getParams() {
        return params;
    }

    @Override
    public boolean shareable() {
        return true; // Implementing the Shareable interface
    }
}
