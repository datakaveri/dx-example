package com.example.book.controller;

import com.example.book.models.Book;
import com.example.book.services.BookService;
import com.example.common.models.response.FailureResponseHandler;
import com.example.common.models.response.ResponseType;
import com.example.common.models.response.SuccessResponseHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BookController extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(BookController.class);
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());  // Add BodyHandler to handle request bodies

        router.get("/books").handler(this::getAllBooks);
        router.post("/books").handler(this::addBook);
        router.put("/books/:id").handler(this::updateBook);
        router.delete("/books/:id").handler(this::deleteBook);
        router.get("/books/:id").handler(this::getBookById);

        server.requestHandler(router).listen(config().getInteger("port", 8082), res -> {
            if (res.succeeded()) {
                LOGGER.info("HTTP server started on port " + config().getInteger("port", 8082));
            } else {
                LOGGER.error("Failed to start HTTP server: " + res.cause().getMessage());
            }
        });
    }

    private void getAllBooks(RoutingContext context) {
        HttpServerResponse response = context.response();
        bookService.getAll().onComplete(ar -> {
            if (ar.succeeded()) {
                List<Book> books = ar.result();
                if (books.isEmpty()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.NoContent.getCode(), (JsonArray) null);
                } else {
                    JsonArray jsonArray = new JsonArray();
                    books.forEach(book -> jsonArray.add(JsonObject.mapFrom(book)));
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), new JsonObject().put("books", jsonArray));
                }
            } else {
                LOGGER.error("Failed to retrieve books: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void addBook(RoutingContext context) {
        HttpServerResponse response = context.response();
        if (context.getBodyAsJson() == null) {
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }
        try {
            Book book = context.getBodyAsJson().mapTo(Book.class);
            bookService.add(book).onComplete(ar -> {
                if (ar.succeeded()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Created.getCode(), JsonObject.mapFrom(ar.result()));
                } else {
                    LOGGER.error("Failed to add book: " + ar.cause().getMessage());
                    FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to parse request body: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, "Invalid JSON body");
        }
    }

    private void getBookById(RoutingContext context) {
        HttpServerResponse response = context.response();
        int id = Integer.parseInt(context.pathParam("id"));
        bookService.getById(id).onComplete(ar -> {
            if (ar.succeeded()) {
                Book book = ar.result();
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(book));
            } else {
                LOGGER.error("Failed to get book: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void updateBook(RoutingContext context) {
        HttpServerResponse response = context.response();
        if (context.getBodyAsJson() == null) {
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }
        int id = Integer.parseInt(context.pathParam("id"));
        try {
            Book book = context.getBodyAsJson().mapTo(Book.class);
            book.setId(id);
            bookService.update(book).onComplete(ar -> {
                if (ar.succeeded()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(ar.result()));
                } else {
                    LOGGER.error("Failed to update book: " + ar.cause().getMessage());
                    FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to parse request body: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, "Invalid JSON body");
        }
    }

    private void deleteBook(RoutingContext context) {
        HttpServerResponse response = context.response();
        int id = Integer.parseInt(context.pathParam("id"));
        bookService.delete(id).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), ar.result());
            } else {
                LOGGER.error("Failed to delete book: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }
}
