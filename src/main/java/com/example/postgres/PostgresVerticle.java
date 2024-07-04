package com.example.postgres;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.postgres.services.PostgresService;
import com.example.postgres.services.PostgresServiceImpl;

import io.vertx.serviceproxy.ServiceBinder;

public class PostgresVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:postgresql://postgres:5432/mydb")
                .put("driver_class", "org.postgresql.Driver")
                .put("user", "user")
                .put("password", "password");

        PostgresService service = new PostgresServiceImpl(vertx, config);
        new ServiceBinder(vertx).setAddress("postgres.service").register(PostgresService.class, service);

        vertx.eventBus().consumer("database.query", this::handleDatabaseQuery);
        vertx.eventBus().consumer("database.update", this::handleDatabaseUpdate);

        startPromise.complete();
    }

    private void handleDatabaseQuery(Message<JsonObject> message) {
        JsonObject queryJson = message.body();
        
        PostgresService service = PostgresService.createProxy(vertx, "postgres.service");
        service.executeQuery(queryJson).onComplete(ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonObject().put("result", new QueryResult(ar.result()).getRows()));
            } else {
                message.fail(500, ar.cause().getMessage());
            }
        });
    }

    private void handleDatabaseUpdate(Message<JsonObject> message) {
        JsonObject queryJson = message.body();
        PostgresService service = PostgresService.createProxy(vertx, "postgres.service");
        service.executeUpdate(queryJson).onComplete(ar -> {
            if (ar.succeeded()) {
                message.reply(new JsonObject().put("success", true));
            } else {
                message.fail(500, ar.cause().getMessage());
            }
        });
    }
}
