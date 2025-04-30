package scoreTracker;

import java.net.*;
import java.io.*;
import java.sql.*;

public class ScoreTrackerServer {

	private static final int PORT = 8000;  // Any free port
    private static Connection conn;
    
    public static void main(String [] args) {
    	try {
            // Connect to the database
            conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db");

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                // Handle each client in a new thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("Received: " + input);

                String[] parts = input.split(" ", 2);
                String command = parts[0];

                switch (command) {
                    case "CREATE_ACCOUNT":
                        out.println(createAccount(parts[1]));
                        break;
                    case "LOGIN":
                        out.println(login(parts[1]));
                        break;
                    case "GET_COMPETITIONS":
                        out.println(getCompetitions(parts[1]));
                        break;
                    case "ADD_COMPETITION":
                        out.println(addCompetition(parts[1]));
                        break;
                    default:
                        out.println("ERROR Unknown command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String createAccount(String data) {
        try {
            String[] parts = data.split(" ");
            String username = parts[0];
            String password = parts[1];

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Users (username, password) VALUES (?, ?)"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            return "SUCCESS Account created";
        } catch (SQLException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private static String login(String data) {
        try {
            String[] parts = data.split(" ");
            String username = parts[0];
            String password = parts[1];

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM Users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "SUCCESS Logged in";
            } else {
                return "ERROR Invalid credentials";
            }
        } catch (SQLException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private static String getCompetitions(String username) {
        try {
            PreparedStatement userStmt = conn.prepareStatement(
                "SELECT id FROM Users WHERE username = ?"
            );
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) return "ERROR User not found";

            int userId = userRs.getInt("id");

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT name FROM Competitions WHERE user_id = ?"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("name")).append(";");
            }

            return "COMPETITIONS " + sb.toString();
        } catch (SQLException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private static String addCompetition(String data) {
        try {
            String[] parts = data.split(" ", 2);
            String username = parts[0];
            String competitionName = parts[1];

            PreparedStatement userStmt = conn.prepareStatement(
                "SELECT id FROM Users WHERE username = ?"
            );
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();

            if (!userRs.next()) return "ERROR User not found";

            int userId = userRs.getInt("id");

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Competitions (name, user_id) VALUES (?, ?)"
            );
            stmt.setString(1, competitionName);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            return "SUCCESS Competition added";
        } catch (SQLException e) {
            return "ERROR " + e.getMessage();
        }
    }
}
