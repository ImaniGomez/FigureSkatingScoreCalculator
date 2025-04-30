package scoreTracker;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class NetworkServer {
	
	private static Connection connection;
	
	public static void main(String [] args) throws Exception{
		connection = DriverManager.getConnection("jdbc:sqlite:skating_scores.db");
		
		ServerSocket serverSocket = new ServerSocket(8000);
		System.out.println("Server running on port 8000");
		
		while(true) {
			Socket clientSocket = serverSocket.accept();
			new Thread(() -> handleClient(clientSocket)).start();
		}
	}
	
	private static void handleClient(Socket socket) {
		try (
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
	        ) {
	            String command;
	            while ((command = in.readLine()) != null) {
	                out.println(processCommand(command));
	            }
	        } catch (IOException e) {
	            //e.printStackTrace();
	        }
	    }

	    private static String processCommand(String commandLine) {
	        try {
	            String[] parts = commandLine.split(" ");
	            String cmd = parts[0];
	            switch (cmd) {
	                case "CREATE_ACCOUNT":
	                    return createAccount(parts[1], parts[2]);
	                case "LOGIN":
	                    return login(parts[1], parts[2]);
	                case "ADD_COMPETITION":
	                    return addCompetition(parts[1], parts[2]);
	                case "GET_COMPETITIONS":
	                    return getCompetitions(parts[1]);
	                default:
	                    return "ERROR Unknown command";
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "ERROR processing command";
	        }
	    }

	    private static String createAccount(String username, String password) throws SQLException {
	        PreparedStatement ps = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
	        ps.setString(1, username);
	        ps.setString(2, password);
	        try {
	            ps.executeUpdate();
	            return "OK Account created";
	        } catch (SQLException e) {
	            return "ERROR Username already exists";
	        }
	    }
	    
	    private static String login(String username, String password) throws SQLException {
	        PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
	        ps.setString(1, username);
	        ps.setString(2, password);
	        ResultSet rs = ps.executeQuery();
	        return rs.next() ? "OK Login successful" : "ERROR Invalid credentials";
	    }

	    private static String addCompetition(String username, String competition) throws SQLException {
	        PreparedStatement ps = connection.prepareStatement("INSERT INTO competitions (username, competition) VALUES (?, ?)");
	        ps.setString(1, username);
	        ps.setString(2, competition);
	        ps.executeUpdate();
	        return "OK Competition added";
	    }

	    private static String getCompetitions(String username) throws SQLException {
	        PreparedStatement ps = connection.prepareStatement("SELECT competition FROM competitions WHERE username = ?");
	        ps.setString(1, username);
	        ResultSet rs = ps.executeQuery();
	        List<String> comps = new ArrayList<>();
	        while (rs.next()) {
	            comps.add(rs.getString("competition"));
	        }
	        return "OK " + String.join(",", comps);
	    }


}
