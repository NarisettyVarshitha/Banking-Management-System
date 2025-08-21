package com.learnJDBC;
import java.sql.*;
import java.util.Scanner;

public class Accounts {
    private final Connection connection;
    private final Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void createAccount(int userId) {
        try {
            // Generate a unique account number (10 digits)
            String accountNumber = String.valueOf(System.currentTimeMillis()).substring(3); 
            // This uses current time to make it unique

            System.out.print("Enter initial balance: ");
            double balance = scanner.nextDouble();
            scanner.nextLine(); // clear newline

            String insert = "INSERT INTO accounts (account_number, user_id, balance) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insert);
            pstmt.setString(1, accountNumber);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, balance);

            pstmt.executeUpdate();
            System.out.println("✅ Account created successfully! Account Number: " + accountNumber);

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("❌ Duplicate account number. Try again.");
        } catch (SQLException e) {
            System.out.println("❌ Database error: " + e.getMessage());
        }
    }


    public void depositAmount() {
        try {
            System.out.print("Enter Account Number: ");
            String accountNumber = scanner.next().trim();

            // Get account_id from account_number
            String getIdSql = "SELECT account_id FROM accounts WHERE account_number = ?";
            PreparedStatement getIdStmt = connection.prepareStatement(getIdSql);
            getIdStmt.setString(1, accountNumber);
            ResultSet rs = getIdStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Account number not found.");
                return;
            }
            int accountId = rs.getInt("account_id");

            System.out.print("Enter Amount to Deposit: ");
            double amount = scanner.nextDouble();

            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Deposit successful!");
            } else {
                System.out.println("Failed to deposit. Try again.");
            }
        } catch (SQLException e) {
            System.err.println("Error depositing amount: " + e.getMessage());
        }
    }

    public void withdrawAmount() {
        try {
            System.out.print("Enter Account Number: ");
            String accountNumber = scanner.next().trim();

            // Get account_id
            String getIdSql = "SELECT account_id, balance FROM accounts WHERE account_number = ?";
            PreparedStatement getIdStmt = connection.prepareStatement(getIdSql);
            getIdStmt.setString(1, accountNumber);
            ResultSet rs = getIdStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Account number not found.");
                return;
            }

            int accountId = rs.getInt("account_id");
            double balance = rs.getDouble("balance");

            System.out.print("Enter Amount to Withdraw: ");
            double amount = scanner.nextDouble();

            if (balance >= amount) {
                String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setDouble(1, amount);
                stmt.setInt(2, accountId);
                stmt.executeUpdate();
                System.out.println("Withdrawal successful!");
            } else {
                System.out.println("Insufficient balance.");
            }

        } catch (SQLException e) {
            System.err.println("Error withdrawing amount: " + e.getMessage());
        }
    }

    public double getBalance(String accountNumber) {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    System.out.println("Account not found!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

   
}
