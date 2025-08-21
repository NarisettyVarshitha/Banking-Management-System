package com.learnJDBC;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Users {
    private Connection conn;
    private Scanner sc;

    public Users(Connection conn, Scanner sc) {
        this.conn = conn;
        this.sc = sc;
    }

    // Validate Email
    private boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    // Validate Phone
    private boolean isValidPhone(String phone) {
        return Pattern.matches("\\d{10,15}", phone);
    }

    // Register new user
    public void registerUser() {
        try {
            System.out.print("Enter full name: ");
            String fullName = sc.next().trim();

            System.out.print("Enter email: ");
            String email = sc.next().trim();
            if (!isValidEmail(email)) {
                System.out.println("❌ Invalid email format.");
                return;
            }

            System.out.print("Enter phone: ");
            String phone = sc.next().trim();
            if (!isValidPhone(phone)) {
                System.out.println("❌ Phone must be 10-15 digits.");
                return;
            }

            System.out.print("Enter password: ");
            String password = sc.next().trim();
            if (password.length() < 6) {
                System.out.println("❌ Password must be at least 6 characters.");
                return;
            }

            String insertQuery = "INSERT INTO Users (full_name, email, phone, password_hash) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, password); // In real app: hash it before saving

            pstmt.executeUpdate();
            System.out.println("✅ User registered successfully!");

        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getMessage().contains("users.email")) {
                System.out.println("❌ Email already exists.");
            } else if (e.getMessage().contains("users.phone")) {
                System.out.println("❌ Phone number already exists.");
            } else {
                System.out.println("❌ Duplicate entry.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    // Login user
    public void loginUser() {
        try {
            System.out.print("Enter full name: ");
            String fullName = sc.next().trim();

            System.out.print("Enter password: ");
            String pwd = sc.next().trim();

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE full_name = ? AND password_hash = ?");
            stmt.setString(1, fullName);
            stmt.setString(2, pwd);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Login successful!");
            } else {
                System.out.println("❌ Invalid name or password.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    // View user profile
    public void viewUserProfile() {
        try {
            System.out.print("Enter User ID: ");
            int userId = sc.nextInt();
            sc.nextLine(); // consume newline

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("User ID: " + rs.getInt("user_id"));
                System.out.println("Full Name: " + rs.getString("full_name"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Created At: " + rs.getTimestamp("created_at"));
            } else {
                System.out.println("❌ User not found.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }

    // Update user details
    public void updateUserDetails() {
        try {
            System.out.print("Enter User ID: ");
            int userId = sc.nextInt();
            System.out.print("Enter new email: ");
            String email = sc.next().trim();
            if (!isValidEmail(email)) {
                System.out.println("❌ Invalid email format.");
                return;
            }

            System.out.print("Enter new password: ");
            String pwd = sc.next().trim();
            if (pwd.length() < 6) {
                System.out.println("❌ Password must be at least 6 characters.");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET email = ?, password_hash = ? WHERE user_id = ?");
            stmt.setString(1, email);
            stmt.setString(2, pwd);
            stmt.setInt(3, userId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ User details updated successfully!");
            } else {
                System.out.println("❌ User not found.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("users.email")) {
                System.out.println("❌ Email already exists.");
            } else {
                System.out.println("❌ Database error: " + e.getMessage());
            }
        }
    }
    public int loginUserAndGetId() {
        try {
            System.out.print("Enter full name: ");
            String fullName = sc.next().trim();

            System.out.print("Enter password: ");
            String pwd = sc.next().trim();

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id FROM users WHERE full_name = ? AND password_hash = ?");
            stmt.setString(1, fullName);
            stmt.setString(2, pwd);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                System.out.println("✅ Login successful!");
                return userId;
            } else {
                System.out.println("❌ Invalid name or password.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
        return -1;  // login failed
    }

}
