package com.example.user.controller;

import com.example.user.models.User;
import com.example.user.services.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import com.example.book.models.Book;
import com.example.book.services.BookService;

import java.util.List;

public class UserController extends AbstractVerticle {

  private UserService userService = new UserService();

  @Override
  public void start() {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    router.get("/books").handler(this::getAllUsers);
    router.post("/books").handler(this::addUser);

    server.requestHandler(router).listen(config().getInteger("port", 8082));
  }
// Example content for src/main/java/com/example/user/controller/UserController.java
private void getAllUsers(Future<List<User>>) {
  userService.getAllUsers(ar -> {
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

  private void addUser(User user) {
    User user = userService.getBodyAsJson().mapTo(User.class);
    userService.addUser(user, ar -> {
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
