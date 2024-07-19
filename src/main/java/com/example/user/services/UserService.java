package com.example.user.services;

import com.example.user.models.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface UserService {

    Future<List<User>> getAll();

    Future<User> add(User user);
    Future<User> update(User user);
    Future<JsonObject> delete(int id);
    Future<User> getById(int id);
    Future<JsonArray> getUsersWithBooks();
    Future<Void> addUserBookRelation(int userId, int bookId);
    Future<List<JsonObject>> getUsersWithBookCount();
}
