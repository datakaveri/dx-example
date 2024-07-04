package com.example.book.services;

import com.example.book.models.Book;
import io.vertx.core.Future;

import java.util.List;

public interface BookService {

    Future<List<Book>> getAll();

    Future<Void> add(Book book);
}
