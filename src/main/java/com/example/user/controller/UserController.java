package com.example.user.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

import com.example.user.models.User;
import com.example.user.services.UserService;

public class UserController extends AbstractVerticle {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/users").handler(this::getAllUsers);
        router.post("/users").handler(this::addUser);

        server.requestHandler(router).listen(config().getInteger("port", 8081));
    }

    private void getAllUsers(RoutingContext context) {
        userService.getAll().onComplete(ar -> {
            if (ar.succeeded()) {
                List<User> users = ar.result();
                JsonArray jsonArray = new JsonArray(users);
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(jsonArray.encode());
            } else {
                context.response()
                        .setStatusCode(500)
                        .end(ar.cause().getMessage());
            }
        });
    }

    private void addUser(RoutingContext context) {
        User user = context.getBodyAsJson().mapTo(User.class);
        userService.add(user).onComplete(ar -> {
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
