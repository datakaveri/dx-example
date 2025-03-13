package com.example.postgres.services;

import com.example.postgres.models.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.core.Promise;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.stream.Collectors;

public class PostgresServiceImpl implements PostgresService {
    private final PgPool client;
    private static final int MAX_LIMIT = 100;

    public PostgresServiceImpl(PgPool client) {
        this.client = client;
    }

    private QueryResult convertToQueryResult(RowSet<Row> rowSet, int totalCount, boolean hasMore) {
        List<JsonObject> rows = rowSet.stream()
                .map(row -> {
                    JsonObject json = new JsonObject();
                    for (int i = 0; i < row.size(); i++) {
                        json.put(row.getColumnName(i), row.getValue(i));
                    }
                    return json;
                })
                .collect(Collectors.toList());
        return new QueryResult(rows, totalCount, hasMore);
    }

    private Future<QueryResult> executeQuery(String sql, List<Object> params, boolean isCountQuery) {
        Promise<QueryResult> promise = Promise.promise();
        client.preparedQuery(sql).execute(Tuple.from(params))
                .onSuccess(result -> {
                    if (isCountQuery) {
                        int totalCount = result.iterator().hasNext() ? result.iterator().next().getInteger(0) : 0;
                        promise.complete(new QueryResult(null, totalCount, false));
                    } else {
                        promise.complete(convertToQueryResult(result, 0, false));
                    }
                })
                .onFailure(error -> promise.complete(new QueryResult(error.getMessage())));
        return promise.future();
    }

    @Override
    public Future<QueryResult> insert(InsertQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams(), false);
    }

    @Override
    public Future<QueryResult> update(UpdateQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams(), false);
    }

    @Override
    public Future<QueryResult> search(SelectQuery query, int limit, int offset) {
        limit = Math.min(limit, MAX_LIMIT);
        String paginatedSQL = query.toSQL() + " LIMIT ? OFFSET ?";
        List<Object> params = query.getQueryParams();
        params.add(limit + 1);
        params.add(offset);

        String countSQL = "SELECT COUNT(*) FROM (" + query.toSQL() + ") AS total";

        return executeQuery(countSQL, query.getQueryParams(), true).compose(countResult -> {
            int totalCount = countResult.getTotalCount();
            return executeQuery(paginatedSQL, params, false).map(result -> {
                boolean hasMore = result.getRows().size() > limit;
                List<JsonObject> rows = result.getRows().stream().limit(limit).collect(Collectors.toList());
                return new QueryResult(rows, totalCount, hasMore);
            });
        });
    }

    @Override
    public Future<QueryResult> delete(DeleteQuery query) {
        return executeQuery(query.toSQL(), query.getQueryParams(), false);
    }
}
