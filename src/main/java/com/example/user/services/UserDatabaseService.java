package com.example.user.services;

import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.user.models.User;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class UserDatabaseService extends AbstractDatabaseService<User> {

    private final EventBus eventBus;

    public UserDatabaseService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected Future<List<User>> getAll() {
        Future<List<User>> future = Future.future();
        Query query = new Query("SELECT * FROM users", new JsonArray());

        eventBus.<QueryResult>request("database.query", query.toJson(), ar -> {
            if (ar.succeeded()) {
                QueryResult result = ar.result().body();
                List<User> users = fromJsonArray(result.getRows());
                future.complete(users);
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }

    @Override
    protected Future<Void> add(User user) {
        Future<Void> future = Future.future();
        JsonArray params = new JsonArray().add(user.getName()).add(user.getEmail());
        Query query = new Query("INSERT INTO users (name, email) VALUES (?, ?)", params);

        eventBus.<QueryResult>request("database.update", query.toJson(), ar -> {
            if (ar.succeeded()) {
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }

    @Override
    protected User fromJsonObject(JsonObject json) {
        User user = new User();
        user.setId(json.getInteger("id"));
        user.setName(json.getString("name"));
        user.setEmail(json.getString("email"));
        return user;
    }

    @Override
    protected JsonObject toJsonObject(User user) {
        return new JsonObject()
                .put("id", user.getId())
                .put("name", user.getName())
                .put("email", user.getEmail());
    }

    @Override
    protected List<User> fromJsonArray(JsonArray jsonArray) {
        List<User> users = new ArrayList<>();
        jsonArray.forEach(json -> users.add(fromJsonObject((JsonObject) json)));
        return users;
    }

    @Override
    protected JsonArray toJsonArray(List<User> users) {
        JsonArray jsonArray = new JsonArray();
        users.forEach(user -> jsonArray.add(toJsonObject(user)));
        return jsonArray;
    }
}
