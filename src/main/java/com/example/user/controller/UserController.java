package com.example.user.controller;

import com.example.user.models.User;
import com.example.user.services.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
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

        server.requestHandler(router).listen(config().getInteger("port", 8081), result -> {
            if (result.succeeded()) {
                LOGGER.info("HTTP server started on port 8081");
            } else {
                LOGGER.error("Failed to start HTTP server", result.cause());
            }
        });
    }

    private void getAllUsers(RoutingContext context) {
        userService.getAll().onComplete(ar -> {
            if (ar.succeeded()) {
                List<User> users = ar.result();
                JsonArray jsonArray = new JsonArray();
                users.forEach(user -> jsonArray.add(JsonObject.mapFrom(user)));
                sendJsonResponse(context, 200, new JsonObject().put("users", jsonArray));
            } else {
                sendErrorResponse(context, 500, ar.cause().getMessage());
            }
        });
    }

    private void addUser(RoutingContext context) {
        LOGGER.info("Received addUser request");
        LOGGER.info("Headers: " + context.request().headers());
        LOGGER.info("Raw body: " + context.getBodyAsString());

        JsonObject jsonBody = context.getBodyAsJson();
        LOGGER.info("Parsed JSON body: " + jsonBody);

        if (jsonBody == null) {
            sendErrorResponse(context, 400, "Request body is missing or not valid JSON");
            return;
        }

        User user;
        try {
            user = jsonBody.mapTo(User.class);
        } catch (DecodeException e) {
            sendErrorResponse(context, 400, "Unable to decode JSON to User object: " + e.getMessage());
            return;
        }

        userService.add(user).onComplete(ar -> {
            if (ar.succeeded()) {
                sendJsonResponse(context, 201, new JsonObject().put("success", true));
            } else {
                sendErrorResponse(context, 500, ar.cause().getMessage());
            }
        });
    }

    private void sendJsonResponse(RoutingContext context, int statusCode, JsonObject body) {
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
