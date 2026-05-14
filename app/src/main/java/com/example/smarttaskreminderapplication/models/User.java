package com.example.smarttaskreminderapplication.models;

/**
 * User model class representing a registered user.
 * Stores user credentials and profile information in SQLite database.
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String password;

    /**
     * Default constructor required for database operations.
     */
    public User() {
    }

    /**
     * Constructor for creating new user (registration).
     *
     * @param name     User's display name
     * @param email    User's email address (unique)
     * @param password User's password (stored in plain text for demo; use hashing in production)
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Full constructor including ID (for database retrieval).
     *
     * @param id       Unique user ID
     * @param name     User's display name
     * @param email    User's email
     * @param password User's password
     */
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
