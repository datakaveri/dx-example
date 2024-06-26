package com.example.postgres;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;

public class PostgresVerticle extends AbstractVerticle {

    private JDBCClient client;

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:postgresql://postgres:5432/mydb")
                .put("driver_class", "org.postgresql.Driver")
                .put("user", "user")
                .put("password", "password");

        client = JDBCClient.createShared(vertx, config);

        vertx.eventBus().consumer("database.query", this::handleDatabaseQuery);
        vertx.eventBus().consumer("database.update", this::handleDatabaseUpdate);

        startPromise.complete();
    }

    private void handleDatabaseQuery(Message<JsonObject> message) {
        JsonObject queryJson = message.body();
        Query query = new Query(queryJson.getString("sql"), queryJson.getJsonArray("params"));
        executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonObject().put("result", new QueryResult(ar.result().getRows())));
            } else {
                message.fail(500, ar.cause().getMessage());
            }
        });
    }

    private void handleDatabaseUpdate(Message<JsonObject> message) {
        JsonObject queryJson = message.body();
        Query query = new Query(queryJson.getString("sql"), queryJson.getJsonArray("params"));
        executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonObject().put("success", true));
            } else {
                message.fail(500, ar.cause().getMessage());
            }
        });
    }

    private void executeQuery(Query query) {
        client.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.queryWithParams(query.getSql(), query.getParams(), res -> {
                    if (res.succeeded()) {
                        // Complete with QueryResult directly
                        query.getPromise().complete(new QueryResult(res.result().getRows()));
                    } else {
                        query.getPromise().fail(res.cause());
                    }
                    connection.close();
                });
            } else {
                query.getPromise().fail(ar.cause());
            }
        });
    }

    private void executeUpdate(Query query) {
        client.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection connection = ar.result();
                connection.updateWithParams(query.getSql(), query.getParams(), res -> {
                    if (res.succeeded()) {
                        query.getPromise().complete();
                    } else {
                        query.getPromise().fail(res.cause());
                    }
                    connection.close();
                });
            } else {
                query.getPromise().fail(ar.cause());
            }
        });
    }
}
