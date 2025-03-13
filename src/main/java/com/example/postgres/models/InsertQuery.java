package com.example.postgres.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertQuery implements QueryModel {
    private String table;
    private Map<String, Object> values;

    public InsertQuery(String table, Map<String, Object> values) {
        this.table = table;
        this.values = values;
    }

    @Override
    public String toSQL() {
        String columns = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    @Override
    public List<Object> getQueryParams() {
        return List.copyOf(values.values());
    }
}
