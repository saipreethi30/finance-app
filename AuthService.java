package com.smartfinance.service;

import com.smartfinance.dao.CategoryDAO;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.User;
import com.smartfinance.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles user registration and login (multi-user support).
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private User currentUser;

    public Optional<User> login(String username, String password) {
        try {
            Optional<User> user = userDAO.findByUsername(username.trim());
            if (user.isPresent() && PasswordUtil.verify(password, user.get().getPasswordHash())) {
                currentUser = user.get();
                return Optional.of(currentUser);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed", e);
        }
        return Optional.empty();
    }

    public User register(String username, String password, String fullName) {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new IllegalArgumentException("Username required and password must be at least 4 characters");
        }
        try {
            if (userDAO.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            User user = new User(username.trim(), PasswordUtil.hash(password), fullName.trim());
            userDAO.insert(user);
            categoryDAO.createDefaultsForUser(user.getId());
            currentUser = user;
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Registration failed", e);
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
