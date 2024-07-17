package com.example.common.database;

import com.example.book.models.Book;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public abstract class AbstractDatabaseService<T> {

    protected abstract Future<List<T>> getAll();

    protected abstract Future<T> add(T item);
    protected abstract Future<T> update(T item);
    protected abstract Future<JsonObject> delete(int id);
    protected abstract Future<T> getById(int id);

    protected abstract JsonObject toJsonObject(T item);

    protected abstract T fromJsonObject(JsonObject json);

    protected abstract JsonArray toJsonArray(List<T> items);

    protected abstract List<T> fromJsonArray(JsonArray jsonArray);
}
