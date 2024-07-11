package com.example.book.controller;

import com.example.book.models.Book;
import com.example.book.services.BookService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
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

        server.requestHandler(router).listen(config().getInteger("port", 8082), res -> {
            if (res.succeeded()) {
                LOGGER.info("HTTP server started on port " + config().getInteger("port", 8082));
            } else {
                LOGGER.error("Failed to start HTTP server: " + res.cause().getMessage());
            }
        });
    }

    private void getAllBooks(RoutingContext context) {
        
        bookService.getAll().onComplete(ar -> {
            if (ar.succeeded()) {
                List<Book> books = ar.result();
                JsonArray jsonArray = new JsonArray(books);
                sendJsonResponse(context, 200, jsonArray);
            } else {
                LOGGER.error("Failed to retrieve books: " + ar.cause().getMessage());
                sendErrorResponse(context, 500, ar.cause().getMessage());
            }
        });
    }

    private void addBook(RoutingContext context) {
        try {
            Book book = context.getBodyAsJson().mapTo(Book.class);
            bookService.add(book).onComplete(ar -> {
                if (ar.succeeded()) {
                    sendJsonResponse(context, 201, new JsonObject().put("success", true));
                } else {
                    LOGGER.error("Failed to add book: " + ar.cause().getMessage());
                    sendErrorResponse(context, 500, ar.cause().getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to parse request body: " + e.getMessage());
            sendErrorResponse(context, 400, "Invalid JSON body");
        }
    }

    private void sendJsonResponse(RoutingContext context, int statusCode, JsonObject body) {
        context.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(body.encode());
    }

    private void sendJsonResponse(RoutingContext context, int statusCode, JsonArray body) {
        context.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(body.encode());
    }

    private void sendErrorResponse(RoutingContext context, int statusCode, String message) {
        JsonObject errorResponse = new JsonObject()
                .put("error", true)
                .put("message", message);

        context.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(errorResponse.encode());
    }
}
