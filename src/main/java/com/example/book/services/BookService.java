package com.example.book.services;

import com.example.book.models.Book;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface BookService {

    Future<List<Book>> getAll();

    Future<Book> add(Book book);
    Future<Book> update(Book book);
    Future<JsonObject> delete(int id);
    Future<Book> getById(int id);
}
