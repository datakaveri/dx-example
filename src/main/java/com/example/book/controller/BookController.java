package com.example.book.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.example.book.models.Book;
import com.example.book.services.BookService;

public class BookController extends AbstractVerticle {

    private BookService bookService = new BookService();

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/books").handler(this::getAllBooks);
        router.post("/books").handler(this::addBook);

        server.requestHandler(router).listen(config().getInteger("port", 8082));
    }

    private void getAllBooks(RoutingContext context) {
        bookService.getAllBooks(ar -> {
            if (ar.succeeded()) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(ar.result().encode());
            } else {
                context.response()
                        .setStatusCode(500)
                        .end(ar.cause().getMessage());
            }
        });
    }

    private void addBook(RoutingContext context) {
        Book book = context.getBodyAsJson().mapTo(Book.class);
        bookService.addBook(book, ar -> {
            if (ar.succeeded()) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("success", true).encode());
            } else {
                context.response()
                        .setStatusCode(500)
                        .end(ar.cause().getMessage());
            }
        });
    }
}
