package com.learnJDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Transactions {
	
	private Connection connection;//here we are using encapsulation
	public void recordTransaction(String type, double amount) {
        System.out.println("Recording Transaction: " + type + " of " + amount);
        // Save to DB
    }
	public void viewTransactionByAccount() throws SQLException {
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("Enter Account Id: ");
			int accountId = sc.nextInt();
			String query = "Select * from transactions where account_id = ? ";
			try (PreparedStatement stmt = connection.prepareStatement(query)){
				
				stmt.setInt(1, accountId);
				ResultSet rs = stmt.executeQuery();
				System.out.println("\\n=== Transactions for Account \" + accountId + \" ===");
				System.out.println("ID | Type | Amount | Date | Description");
			    System.out.println("-----------------------------------------------");
			    while(rs.next()) {
			    	System.out.println(
			    			rs.getInt("transaction_id") + " | " +
			    	        rs.getString("type") + " | " +
			    	        rs.getDouble("amount") + " | " +
			    	        rs.getTimestamp("transaction_date") + " | " +
			    	        rs.getString("description")
			    			);
			    }
			}
			catch (SQLException e){
			    e.printStackTrace();
			}
		}
		
	}
	
	public void viewTransactionByDate() throws SQLException{
		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("Enter Start Date( YYYY-MM-DD): ");
			String startDate = sc.next();
			System.out.println("Enter End Date( YYYY-MM-DD): ");
			String endDate = sc.nextLine();
			String query = "Select  * from  transactions where transaction_date between ? AND ? ";
			try(PreparedStatement stmt = connection.prepareStatement(query)){
				stmt.setString(1, startDate + "00 :00 : 00");
				stmt.setString(2, endDate+"23 : 59 : 59");
				ResultSet rs = stmt.executeQuery();
				System.out.println("\n=== Transactions from " + startDate + " to " + endDate + " ===");
			    System.out.println("ID | Account ID | Type | Amount | Date | Description");
			    System.out.println("----------------------------------------------------");

			    while (rs.next()) {
			        System.out.println(
			            rs.getInt("transaction_id") + " | " +
			            rs.getInt("account_id") + " | " +
			            rs.getString("type") + " | " +
			            rs.getDouble("amount") + " | " +
			            rs.getTimestamp("transaction_date") + " | " +
			            rs.getString("description")
			        );
			    }
			}
			catch (SQLException e){
			    e.printStackTrace();
			}
		}
	}

}
