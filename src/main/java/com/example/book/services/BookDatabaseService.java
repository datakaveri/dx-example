package com.example.book.services;

import com.example.book.models.Book;
import com.example.common.database.AbstractDatabaseService;
import com.example.common.models.Query;
import com.example.common.models.QueryResult;
import com.example.common.models.response.ResponseBuilder;
import com.example.postgres.services.PostgresService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.example.common.models.response.HttpStatusCode.NOT_FOUND;
import static com.example.common.models.response.HttpStatusCode.NO_CONTENT;

public class BookDatabaseService extends AbstractDatabaseService<Book> implements BookService {
    private static final Logger LOGGER = LogManager.getLogger(BookDatabaseService.class);

    private final PostgresService postgresService;
    private ResponseBuilder responseBuilder;

    public BookDatabaseService(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    @Override
    public Future<List<Book>> getAll() {
        Promise<List<Book>> promise = Promise.promise();
        Query query = new Query("SELECT * FROM books", new JsonArray());

        postgresService.executeQuery(query).onComplete(ar -> {
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
    public Future<Book> add(Book book) {
        Promise<Book> promise = Promise.promise();
        JsonArray params = new JsonArray().add(book.getTitle()).add(book.getAuthor());
        Query query = new Query("INSERT INTO books (title, author) VALUES (?, ?)", params);

        postgresService.executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(book);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<Book> update(Book book) {
        Promise<Book> promise = Promise.promise();
        JsonArray params = new JsonArray().add(book.getTitle()).add(book.getAuthor()).add(book.getId());
        Query query = new Query("UPDATE books SET title = ?, author = ? WHERE id = ?", params);

        postgresService.executeUpdate(query).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(book);
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    @Override
    public Future<JsonObject> delete(int id) {
        Promise<JsonObject> promise = Promise.promise();

        // Check if the book exists before attempting to delete
        getById(id).onComplete(ar -> {
            if (ar.succeeded()) {
                //Book book = ar.result();
                JsonArray params = new JsonArray().add(id);
                Query query = new Query("DELETE FROM books WHERE id = ?", params);

                postgresService.executeUpdate(query).onComplete(deleteResult -> {
                    if (deleteResult.succeeded()) {
                        promise.complete(new JsonObject().put("id", id));
                    } else {
                        LOGGER.debug("Failed to delete book with id: " + id, deleteResult.cause());
                        promise.fail(deleteResult.cause());
                    }
                });
            } else {
                LOGGER.debug("Failed to check if book exists before deletion", ar.cause());
                responseBuilder =
                        new ResponseBuilder().setTypeAndTitle(404).setMessage(NOT_FOUND.getDescription());
                promise.fail(responseBuilder.getResponse().toString());
            }
        });

        return promise.future();
    }


    @Override
    public Future<Book> getById(int id) {
        Promise<Book> promise = Promise.promise();
        JsonArray params = new JsonArray().add(id);
        Query query = new Query("SELECT * FROM books WHERE id = ?", params);

        postgresService.executeQuery(query).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject res = ar.result();
                if (res.getJsonArray("rows").isEmpty()) {
                    responseBuilder =
                            new ResponseBuilder().setTypeAndTitle(204).setMessage(NO_CONTENT.getDescription());
                    promise.fail(responseBuilder.getResponse().toString());
                } else {
                    LOGGER.info("res: " + res);
                    promise.complete(res.getJsonArray("rows").getJsonObject(0).mapTo(Book.class));
                }
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
