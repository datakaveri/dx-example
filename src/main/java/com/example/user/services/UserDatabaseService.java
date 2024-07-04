package com.example.user.services;

import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.postgres.services.PostgresService;
import com.example.user.models.User;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class UserDatabaseService extends AbstractDatabaseService<User> implements UserService {

    private final PostgresService postgresService;

    public UserDatabaseService(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    @Override
    public Future<List<User>> getAll() {
        Promise<List<User>> promise = Promise.promise();
        Query query = new Query("SELECT * FROM users", new JsonArray());

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = ar.result();
                List<User> users = fromJsonArray(result.getRows());
                promise.complete(users);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<Void> add(User user) {
        Promise<Void> promise = Promise.promise();
        JsonArray params = new JsonArray().add(user.getName()).add(user.getEmail());
        Query query = new Query("INSERT INTO users (name, email) VALUES (?, ?)", params);

        postgresService.executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
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
