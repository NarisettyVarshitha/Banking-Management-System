package com.learnJDBC;

import java.sql.*;
import java.util.Scanner;

public class BankSystem {
    private static final String url = "jdbc:mysql://localhost:3306/bms";
    private static final String username = "root";//type_your_username
    private static final String password = "type_your_password";

    private Connection conn;
    private Scanner sc;
    private int loggedInUserId = -1;  // store logged-in user's id here
  


    public BankSystem(Connection conn, Scanner sc) {
        this.conn = conn;
        this.sc = sc;
    }
  
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in);
             Connection conn = DriverManager.getConnection(url, username, password)) {

            BankSystem bankSystem = new BankSystem(conn, sc);
            Accounts accounts = new Accounts(conn, sc);
            Users users = new Users(conn, sc);

            while (true) {
                System.out.println("\n===== Bank System Menu =====");
                System.out.println("1. Create Account");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Check Balance");
                System.out.println("5. Transfer Funds");
                System.out.println("6. Generate Account Statement");
                System.out.println("7. Register User");
                System.out.println("8. Login User");
                System.out.println("9. View User Profile");
                System.out.println("10. Update User Details");
                System.out.println("11. Exit");
                System.out.print("Enter your choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                case 1 -> {
                    bankSystem.createAccountForLoggedInUser();
                }

                    case 2 -> accounts.depositAmount();
                    case 3 -> accounts.withdrawAmount();
                    case 4 -> {
                        System.out.print("Enter your Account Number: ");
                        String accountNumber = sc.next().trim();
                        double balance = accounts.getBalance(accountNumber);
                        System.out.println("Current Balance: " + balance);
                    }

                    case 5 -> bankSystem.transferFunds();
                    case 6 -> bankSystem.generateAccountStatement();
                    case 7 -> users.registerUser();
                    case 8 -> {
                        int userId = users.loginUserAndGetId();
                        if (userId != -1) {
                            bankSystem.setLoggedInUserId(userId);
                        }
                    }

                    case 9 -> users.viewUserProfile();
                    case 10 -> users.updateUserDetails();
                    case 11 -> {
                        System.out.println("Exiting... Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createAccountForLoggedInUser() {
        if (loggedInUserId == -1) {
            System.out.println("❌ You must login first before creating an account.");
            return;
        }
        Accounts accounts = new Accounts(conn, sc);

        accounts.createAccount(loggedInUserId);
    }
    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    // Transfer funds method
    public void transferFunds() {
        try {
            System.out.print("Enter your Account Number: ");
            String fromAccountNumber = sc.next().trim();
            System.out.print("Enter recipient Account Number: ");
            String toAccountNumber = sc.next().trim();
            System.out.print("Enter amount to transfer: ");
            double amount = sc.nextDouble();

            // Get fromAccountId
            int fromAccountId = getAccountIdFromNumber(fromAccountNumber);
            if (fromAccountId == -1) {
                System.out.println("Your account number not found.");
                return;
            }

            // Get toAccountId
            int toAccountId = getAccountIdFromNumber(toAccountNumber);
            if (toAccountId == -1) {
                System.out.println("Recipient account number not found.");
                return;
            }

            conn.setAutoCommit(false);

            PreparedStatement withdrawStmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
            withdrawStmt.setDouble(1, amount);
            withdrawStmt.setInt(2, fromAccountId);
            withdrawStmt.executeUpdate();

            PreparedStatement depositStmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
            depositStmt.setDouble(1, amount);
            depositStmt.setInt(2, toAccountId);
            depositStmt.executeUpdate();

            conn.commit();
            System.out.println("Transfer successful!");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private int getAccountIdFromNumber(String accountNumber) throws SQLException {
        String query = "SELECT account_id FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("account_id");
            } else {
                return -1;
            }
        }
    }

    // Generate account statement
    public void generateAccountStatement() {
        try {
            System.out.print("Enter your Account ID: ");
            long accountId = sc.nextLong();
            sc.nextLine(); // clear newline left in scanner buffer

            // Call Transactions class method to show transactions
            viewTransactionByAccount(accountId);

        } catch (Exception e) {
            System.out.println("❌ Error generating account statement.");
            e.printStackTrace();
        }
    }

    
    public void viewTransactionByAccount(long accountId) {
        String query = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, accountId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n=== Transactions for Account ID: " + accountId + " ===");
            System.out.println("ID | Type | Amount | Date | Description");
            System.out.println("-------------------------------------------------------------");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                System.out.printf(
                    "%d | %s | %.2f | %s | %s%n",
                    rs.getInt("transaction_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("transaction_date"),
                    rs.getString("description")
                );
            }

            if (!hasResults) {
                System.out.println("No transactions found for this account.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error retrieving transactions.");
            e.printStackTrace();
        }
    }


    
    
}
