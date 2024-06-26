package com.example.book.services;

import com.example.book.models.Book;
import io.vertx.core.Future;

import java.util.List;

public interface BookService {

    Future<List<Book>> getAllBooks();

    Future<Void> addBook(Book book);
}
