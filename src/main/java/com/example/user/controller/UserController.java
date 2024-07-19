package com.example.user.controller;

import com.example.common.models.response.FailureResponseHandler;
import com.example.common.models.response.ResponseType;
import com.example.common.models.response.SuccessResponseHandler;
import com.example.user.models.User;
import com.example.user.services.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class UserController extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());  // Add BodyHandler to handle request bodies

        router.get("/users").handler(this::getAllUsers);
        router.post("/users").handler(this::addUser);
        router.put("/users/:id").handler(this::updateUser);
        router.delete("/users/:id").handler(this::deleteUser);
        router.get("/users/:id").handler(this::getUserById);
        router.post("/users-books").handler(this::addUserBookRelation);
        router.get("/users-with-books").handler(this::getUsersWithBooks);
        router.get("/users/books/count").handler(this::getUsersWithBookCount);

        server.requestHandler(router).listen(config().getInteger("port", 8081), result -> {
            if (result.succeeded()) {
                LOGGER.info("HTTP server started on port 8081");
            } else {
                LOGGER.error("Failed to start HTTP server", result.cause());
            }
        });
    }

    private void getAllUsers(RoutingContext context) {
        HttpServerResponse response = context.response();
        userService.getAll().onComplete(ar -> {
            if (ar.succeeded()) {
                List<User> users = ar.result();
                if (users.isEmpty()) {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.NoContent.getCode(), (JsonArray) null);
                } else {
                    JsonArray jsonArray = new JsonArray();
                    users.forEach(user -> jsonArray.add(JsonObject.mapFrom(user)));
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), jsonArray);
                }
            } else {
                LOGGER.error("Failed to retrieve users: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void addUser(RoutingContext context) {
        HttpServerResponse response = context.response();
        LOGGER.info("Received addUser request");
        LOGGER.info("Headers: " + context.request().headers());
        LOGGER.info("Raw body: " + context.getBodyAsString());

        JsonObject jsonBody = context.getBodyAsJson();
        LOGGER.info("Parsed JSON body: " + jsonBody);

        if (jsonBody == null) {
            LOGGER.error("Failed to retrieve books: " + "Request body is missing or not valid JSON");
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }

        User user;
        try {
            user = jsonBody.mapTo(User.class);
        } catch (DecodeException e) {
            LOGGER.error("Failed to retrieve books: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, e.getMessage());
            return;
        }

        userService.add(user).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Created.getCode(), JsonObject.mapFrom(ar.result()));
            } else {
                LOGGER.error("Failed to retrieve users: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void updateUser(RoutingContext context) {
        HttpServerResponse response = context.response();
        int id = Integer.parseInt(context.pathParam("id"));
        JsonObject jsonBody = context.getBodyAsJson();

        if (jsonBody == null) {
            LOGGER.error("Request body is missing or not valid JSON");
            FailureResponseHandler.processBackendResponse(response, "Request body is missing or not valid JSON");
            return;
        }

        User user;
        try {
            user = jsonBody.mapTo(User.class);
        } catch (DecodeException e) {
            LOGGER.error("Failed to parse JSON body: " + e.getMessage());
            FailureResponseHandler.processBackendResponse(response, e.getMessage());
            return;
        }

        user.setId(id);
        userService.update(user).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(ar.result()));
            } else {
                LOGGER.error("Failed to update user: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void deleteUser(RoutingContext context) {
        HttpServerResponse response = context.response();
        int id = Integer.parseInt(context.pathParam("id"));
        userService.delete(id).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), ar.result());
            } else {
                LOGGER.error("Failed to delete user: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void getUserById(RoutingContext context) {
        HttpServerResponse response = context.response();
        int id = Integer.parseInt(context.pathParam("id"));
        userService.getById(id).onComplete(ar -> {
            if (ar.succeeded()) {
                User user = ar.result();
                if (user == null) {
                    FailureResponseHandler.processBackendResponse(response, "User not found");
                } else {
                    SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), JsonObject.mapFrom(user));
                }
            } else {
                LOGGER.error("Failed to get user: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

    private void getUsersWithBooks(RoutingContext context) {
        HttpServerResponse response = context.response();
        userService.getUsersWithBooks().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray resultArray = ar.result();
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), resultArray);
            } else {
                LOGGER.error("Failed to retrieve users with books: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }
    private void addUserBookRelation(RoutingContext context) {
        HttpServerResponse response = context.response();
        JsonObject jsonBody = context.getBodyAsJson();

        if (jsonBody == null || !jsonBody.containsKey("user_id") || !jsonBody.containsKey("book_id")) {
            FailureResponseHandler.processBackendResponse(response, "Request body must contain user_id and book_id");
            return;
        }

        int userId = jsonBody.getInteger("user_id");
        int bookId = jsonBody.getInteger("book_id");

        userService.addUserBookRelation(userId, bookId).onComplete(ar -> {
            if (ar.succeeded()) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Created.getCode(), new JsonObject().put("message", "User-book relationship added successfully"));
            } else if (ar.cause().getMessage().equals("User-book relationship already exists")) {
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), new JsonObject().put("message", "User-book relationship already exists"));
            } else {
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }
    private void getUsersWithBookCount(RoutingContext context) {
        HttpServerResponse response = context.response();

        userService.getUsersWithBookCount().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray resultArray = new JsonArray();
                ar.result().forEach(resultArray::add);
                SuccessResponseHandler.handleSuccessResponse(response, ResponseType.Ok.getCode(), resultArray);
            } else {
                LOGGER.error("Failed to retrieve users with book count: " + ar.cause().getMessage());
                FailureResponseHandler.processBackendResponse(response, ar.cause().getMessage());
            }
        });
    }

}
