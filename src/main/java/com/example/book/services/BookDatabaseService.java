package com.example.book.services;

import com.example.book.models.Book;
import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BookDatabaseService extends AbstractDatabaseService<Book> {

    private final EventBus eventBus;

    public BookDatabaseService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected Future<List<Book>> getAll() {
        Future<List<Book>> future = Future.future().otherwiseEmpty();
        Query query = new Query("SELECT * FROM books", new JsonArray());

        eventBus.<QueryResult>request("database.query", query.toJson(), ar -> {
            if (ar.succeeded()) {
                QueryResult result = ar.result().body();
                List<Book> books = fromJsonArray(result.getRows());
                future.complete(books);
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }

    @Override
    protected Future<Void> add(Book book) {
        Future<Void> future = Future.future();
        JsonArray params = new JsonArray().add(book.getTitle()).add(book.getAuthor());
        Query query = new Query("INSERT INTO books (title, author) VALUES (?, ?)", params);

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
    protected Book fromJsonObject(JsonObject json) {
        Book book = new Book();
        book.setId(json.getInteger("id"));
        book.setTitle(json.getString("title"));
        book.setAuthor(json.getString("author"));
        return book;
    }

    @Override
    protected JsonObject toJsonObject(Book book) {
        return new JsonObject()
                .put("id", book.getId())
                .put("title", book.getTitle())
                .put("author", book.getAuthor());
    }

    @Override
    protected List<Book> fromJsonArray(JsonArray jsonArray) {
        List<Book> books = new ArrayList<>();
        jsonArray.forEach(json -> books.add(fromJsonObject((JsonObject) json)));
        return books;
    }

    @Override
    protected JsonArray toJsonArray(List<Book> books) {
        JsonArray jsonArray = new JsonArray();
        books.forEach(book -> jsonArray.add(toJsonObject(book)));
        return jsonArray;
    }
}
