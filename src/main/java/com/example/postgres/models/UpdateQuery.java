package com.example.postgres.models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateQuery implements QueryModel {
    private String table;
    private Map<String, Object> values;
    private List<Filter> filters;
    private List<JoinClause> joins;

    public UpdateQuery(String table, Map<String, Object> values, List<Filter> filters, List<JoinClause> joins) {
        this.table = table;
        this.values = values;
        this.filters = filters;
        this.joins = joins;
    }

    @Override
    public String toSQL() {
        String setClause = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(", "));
        String joinClause = joins.stream().map(JoinClause::toSQL).collect(Collectors.joining(" "));
        String whereClause = filters.stream().map(Filter::toSQL).collect(Collectors.joining(" AND "));
        return "UPDATE " + table + " " + joinClause + " SET " + setClause + " WHERE " + whereClause;
    }

    @Override
    public List<Object> getQueryParams() {
        List<Object> params = List.copyOf(values.values());
        params.addAll(filters.stream().map(Filter::getValue).toList());
        return params;
    }
}
