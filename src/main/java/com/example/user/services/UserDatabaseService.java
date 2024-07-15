package com.example.user.services;

import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.common.models.response.ResponseBuilder;
import com.example.postgres.services.PostgresService;
import com.example.user.models.User;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.common.models.response.HttpStatusCode.CONFLICT;
import static com.example.common.models.response.HttpStatusCode.NOT_FOUND;

public class UserDatabaseService extends AbstractDatabaseService<User> implements UserService {
    private static final Logger LOGGER = LogManager.getLogger(UserDatabaseService.class);

    private final PostgresService postgresService;
    private ResponseBuilder responseBuilder;

    public UserDatabaseService(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    @Override
    public Future<List<User>> getAll() {
        Promise<List<User>> promise = Promise.promise();
        Query query = new Query("SELECT * FROM users", new JsonArray());

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                List<User> users = fromJsonArray(result.getRows());
                promise.complete(users);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<User> add(User user) {
        Promise<User> promise = Promise.promise();
        JsonArray params = new JsonArray().add(user.getName()).add(user.getEmail());
        Query query = new Query("INSERT INTO users (name, email) VALUES (?, ?)", params);

        postgresService.executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(user);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<User> update(User user) {
        Promise<User> promise = Promise.promise();
        JsonArray params = new JsonArray().add(user.getName()).add(user.getEmail()).add(user.getId());
        Query query = new Query("UPDATE users SET name = ?, email = ? WHERE id = ?", params);

        postgresService.executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(user);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<JsonObject> delete(int id) {
        Promise<JsonObject> promise = Promise.promise();
        getById(id).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray params = new JsonArray().add(id);
                Query query = new Query("DELETE FROM users WHERE id = ?", params);

                postgresService.executeUpdate(query).onComplete(deleteResult -> {
                    if (deleteResult.succeeded()) {
                        promise.complete(new JsonObject().put("id", id));
                    } else {
                        promise.fail(deleteResult.cause());
                    }
                });
            } else {
                LOGGER.debug("Failed to check if user exists before deletion", ar.cause());
                responseBuilder =
                        new ResponseBuilder().setTypeAndTitle(404).setMessage(NOT_FOUND.getDescription());
                promise.fail(responseBuilder.getResponse().toString());
            }
        });

        return promise.future();
    }

    @Override
    public Future<User> getById(int id) {
        Promise<User> promise = Promise.promise();
        JsonArray params = new JsonArray().add(id);
        Query query = new Query("SELECT * FROM users WHERE id = ?", params);

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                List<JsonObject> rows = result.getRows().getList();
                if (rows.isEmpty()) {
                    responseBuilder =
                            new ResponseBuilder().setTypeAndTitle(404).setMessage(NOT_FOUND.getDescription());
                    promise.fail(responseBuilder.getResponse().toString());
                } else {
                    promise.complete(rows.get(0).mapTo(User.class));
                }
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<List<JsonObject>> getUsersWithBooks() {
        Promise<List<JsonObject>> promise = Promise.promise();

        String sql = "SELECT users.id AS user_id, users.name, users.email, books.id AS book_id, books.title, books.author " +
                "FROM users " +
                "JOIN users_books ON users.id = users_books.user_id " +
                "JOIN books ON users_books.book_id = books.id";

        Query query = new Query(sql, new JsonArray());

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                List<JsonObject> usersWithBooks = result.getRows().getList();
                promise.complete(usersWithBooks);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<Void> addUserBookRelation(int userId, int bookId) {
        Promise<Void> promise = Promise.promise();
        userBookRelationExists(userId, bookId).onComplete(ar -> {
            if (ar.succeeded()) {
                if (ar.result()) {
                    responseBuilder =
                            new ResponseBuilder().setTypeAndTitle(409).setMessage(CONFLICT.getDescription());
                    promise.fail(responseBuilder.getResponse().toString());
                } else {
                    JsonArray params = new JsonArray().add(userId).add(bookId);
                    Query query = new Query("INSERT INTO users_books (user_id, book_id) VALUES (?, ?)", params);

                    postgresService.executeUpdate(query).onComplete(ar2 -> {
                        if (ar2.succeeded()) {
                            promise.complete();
                        } else {
                            promise.fail(ar2.cause());
                        }
                    });
                }
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }


    private Future<Boolean> userBookRelationExists(int userId, int bookId) {
        Promise<Boolean> promise = Promise.promise();
        JsonArray params = new JsonArray().add(userId).add(bookId);
        Query query = new Query("SELECT 1 FROM users_books WHERE user_id = ? AND book_id = ?", params);

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                promise.complete(result.getRows().size() > 0);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<List<JsonObject>> getUsersWithBookCount() {
        Promise<List<JsonObject>> promise = Promise.promise();
        Query query = new Query(
                "SELECT u.id, u.name, u.email, COUNT(ub.book_id) AS book_count " +
                        "FROM users u " +
                        "LEFT JOIN users_books ub ON u.id = ub.user_id " +
                        "GROUP BY u.id", new JsonArray()
        );

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                List<JsonObject> usersWithBookCount = result.getRows().stream()
                        .map(json -> new JsonObject()
                                .put("id", ((JsonObject) json).getInteger("id"))
                                .put("name", ((JsonObject) json).getString("name"))
                                .put("email", ((JsonObject) json).getString("email"))
                                .put("book_count", ((JsonObject) json).getLong("book_count").intValue())
                        ).collect(Collectors.toList());
                promise.complete(usersWithBookCount);
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
