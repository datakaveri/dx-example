package com.example.user.services;

import com.example.user.models.User;
import io.vertx.core.Future;

import java.util.List;

public interface UserService {

    Future<List<User>> getAllUsers();

    Future<Void> addUser(User user);
}
