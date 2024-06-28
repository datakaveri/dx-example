package com.example.user.models;

public class User {
  private Integer id;
  private String name;
  private String email;

  // Default constructor
  public User() {}

  // Constructor with parameters
  public User(Integer id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  // Getters and setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}// Example content for src/main/java/com/example/user/models/User.java
