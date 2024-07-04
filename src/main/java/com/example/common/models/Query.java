package com.example.common.models;

import io.vertx.core.shareddata.Shareable;
import io.vertx.core.json.JsonArray;

public class Query implements Shareable {

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

    @Override
    public boolean shareable() {
        return true; // Implementing the Shareable interface
    }
}
