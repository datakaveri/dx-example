package com.example.postgres.models;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteQuery implements QueryModel {
    private String table;
    private List<Filter> filters;

    public DeleteQuery(String table, List<Filter> filters) {
        this.table = table;
        this.filters = filters;
    }

    @Override
    public String toSQL() {
        String whereClause = filters.stream().map(Filter::toSQL).collect(Collectors.joining(" AND "));
        return "DELETE FROM " + table + " WHERE " + whereClause;
    }

    @Override
    public List<Object> getQueryParams() {
        return filters.stream().map(Filter::getValue).toList();
    }
}
