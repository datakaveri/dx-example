package com.example.book.services;

import com.example.book.models.Book;
import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.postgres.services.PostgresService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class BookDatabaseService extends AbstractDatabaseService<Book> implements BookService {

    private final PostgresService postgresService;

    public BookDatabaseService(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    @Override
    public Future<List<Book>> getAll() {
        Promise<List<Book>> promise = Promise.promise();
        Query query = new Query("SELECT * FROM books", new JsonArray());

        postgresService.executeQuery(query.toJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                QueryResult result = new QueryResult(ar.result());
                List<Book> books = fromJsonArray(result.getRows());
                promise.complete(books);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<Void> add(Book book) {
        Promise<Void> promise = Promise.promise();
        JsonArray params = new JsonArray().add(book.getTitle()).add(book.getAuthor());
        Query query = new Query("INSERT INTO books (title, author) VALUES (?, ?)", params);

        postgresService.executeUpdate(query.toJson()).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
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
