package com.example.postgres.models;

import java.util.List;
import java.util.stream.Collectors;

public class SelectQuery implements QueryModel {
    private String table;
    private List<String> columns;
    private List<Filter> filters;
    private List<JoinClause> joins;

    public SelectQuery(String table, List<String> columns, List<Filter> filters, List<JoinClause> joins) {
        this.table = table;
        this.columns = columns;
        this.filters = filters;
        this.joins = joins;
    }

    @Override
    public String toSQL() {
        String colString = columns.isEmpty() ? "*" : String.join(", ", columns);
        String joinClause = joins.stream().map(JoinClause::toSQL).collect(Collectors.joining(" "));
        String query = "SELECT " + colString + " FROM " + table + " " + joinClause;
        if (!filters.isEmpty()) {
            query += " WHERE " + filters.stream().map(Filter::toSQL).collect(Collectors.joining(" AND "));
        }
        return query;
    }

    @Override
    public List<Object> getQueryParams() {
        return filters.stream().map(Filter::getValue).toList();
    }
}
