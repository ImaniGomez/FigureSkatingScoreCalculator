package scoreTracker;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class ScoresBySeason extends JPanel {
	private int accountId;
	
	public ScoresBySeason() {
		//this.accountId = accountId;
		
		setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		add(scrollPane, BorderLayout.CENTER);
		
		loadCompetitions(contentPanel);
		
	}
	
	private void loadCompetitions(JPanel contentPanel) {
		System.out.println("5");
	    String url = "jdbc:sqlite:skating_scores.db";
	    
	    try (Connection conn = DriverManager.getConnection(url)) {
	        // SQL query to get competition details and scores
	        String query = """
	            SELECT 
	                c.id AS competition_id,
	                c.name AS competition_name,
	                sp.total_score AS short_program_score,
	                lp.total_score AS free_program_score,
	                (sp.total_score + lp.total_score) AS total_score
	            FROM competitions c
	            LEFT JOIN programs sp ON c.id = sp.competition_id AND sp.type = 'short'
	            LEFT JOIN programs lp ON c.id = lp.competition_id AND lp.type = 'long'
	            WHERE c.account_id = ?;
	        """;

	        PreparedStatement pstmt = conn.prepareStatement(query);
	        pstmt.setInt(1, accountId);  // Ensure accountId is correctly set
	        System.out.println("1");
	        ResultSet rs = pstmt.executeQuery();
	        System.out.println("2");
	        
	        while (rs.next()) {
	        	System.out.println("3");
	            int compId = rs.getInt("competition_id");
	            String competitionName = rs.getString("competition_name");
	            double shortProgramScore = rs.getDouble("short_program_score");
	            double freeProgramScore = rs.getDouble("free_program_score");
	            double totalScore = rs.getDouble("total_score");

	            JPanel compPanel = new JPanel(new BorderLayout());
	            compPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	            compPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
	            System.out.println("4");

	            // Create the label for competition name and scores
	            JLabel compLabel = new JLabel(String.format("%s: Short=%.2f, Free=%.2f, Total=%.2f", 
	                                                        competitionName, shortProgramScore, freeProgramScore, totalScore));
	            compLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

	            // Create the delete button
	            JButton deleteBtn = new JButton("Delete");
	            deleteBtn.addActionListener(new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                    // Use the compId directly here to delete the competition
	                    deleteCompetition(compId);  // Call delete method with the compId
	                }
	            });

	            // Create the toggle button for details
	            JButton toggleBtn = new JButton("▼");

	            // Panel for the header (competition name + delete button + toggle button)
	            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // Using FlowLayout
	            headerPanel.add(compLabel);
	            headerPanel.add(deleteBtn);  // Add delete button here
	            headerPanel.add(toggleBtn);  // Add toggle button here

	            // Create the details panel (score details)
	            JPanel detailsPanel = createProgramDetailsPanel(conn, compId);  
	            detailsPanel.setVisible(false);  // Set initially hidden

	            // Toggle button action to show/hide details
	            toggleBtn.addActionListener(e -> {
	                boolean isVisible = detailsPanel.isVisible();
	                detailsPanel.setVisible(!isVisible);  // Toggle visibility
	                toggleBtn.setText(isVisible ? "▼" : "▲");  // Change the button text
	                this.revalidate();  // Revalidate the parent panel to update the layout
	                this.repaint();  // Repaint the parent panel
	            });

	            // Add panels to the content
	            compPanel.add(headerPanel, BorderLayout.NORTH);
	            compPanel.add(detailsPanel, BorderLayout.CENTER);

	            contentPanel.add(compPanel);
	            contentPanel.revalidate();
	            contentPanel.repaint();
	            
	            System.out.println("Short Score: " + rs.getDouble("short_program_score"));
	            System.out.println("Free Score: " + rs.getDouble("free_program_score"));
	            System.out.println("Total Score: " + rs.getDouble("total_score"));

	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	private JPanel createProgramDetailsPanel(Connection conn, int competitionId) throws SQLException{
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.setBorder(new EmptyBorder(5, 20, 5, 20));
	    
	    JPanel scorePanel = new JPanel(new GridLayout(0, 1));
	    ButtonGroup bg = new ButtonGroup();
	    JRadioButton shortBtn = new JRadioButton("Short Program");
	    JRadioButton longBtn = new JRadioButton("Long Program");
	    bg.add(shortBtn);
	    bg.add(longBtn);
	    
	    JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    radioPanel.add(shortBtn);
	    radioPanel.add(longBtn);
	    panel.add(radioPanel, BorderLayout.NORTH);
	    panel.add(scorePanel, BorderLayout.CENTER);
	    
	    HashMap<String, Double> scores = new HashMap<>();
	    
	    PreparedStatement stmt = conn.prepareStatement("SELECT type, total_score FROM programs WHERE competition_id = ?");
	    stmt.setInt(1,  competitionId);
	    ResultSet rs = stmt.executeQuery();
	    boolean scoresFound = false;
	    
	    Double shortProgramScore = null;
	    Double longProgramScore = null;
	    
	    while(rs.next()) {
	        String programType = rs.getString("type");
	        Double score = rs.getDouble("total_score");
	        
	        scores.put(programType, score);

	        if ("short".equals(programType)) {
	            shortProgramScore = score;
	        } else if ("long".equals(programType)) {
	            longProgramScore = score;
	        }
	    }

	    // Ensure you display both program scores if they exist
	    if (shortProgramScore != null) {
	        scorePanel.add(new JLabel("Short Program Score: " + shortProgramScore));
	    }
	    if (longProgramScore != null) {
	        scorePanel.add(new JLabel("Long Program Score: " + longProgramScore));
	    }
	    
	    if (!scoresFound) {
	        System.out.println("No scores found for competition ID: " + competitionId);
	    }
	    
	    // Add listeners for the radio buttons
	    shortBtn.addActionListener(e -> {
	        scorePanel.removeAll();
	        scorePanel.add(new JLabel("Short Program Score: " + scores.getOrDefault("short", 0.0)));
	        scorePanel.revalidate();
	        scorePanel.repaint();
	    });
	    
	    longBtn.addActionListener(e -> {
	        scorePanel.removeAll();
	        scorePanel.add(new JLabel("Long Program Score: " + scores.getOrDefault("long", 0.0)));
	        scorePanel.revalidate();
	        scorePanel.repaint();
	    });
	    
	    shortBtn.setSelected(true);
	    shortBtn.doClick();
	    
	    return panel;
	}
	
	
	private void deleteCompetition(int competitionId) {
	    String url = "jdbc:sqlite:skating_scores.db";
	    
	    try (Connection conn = DriverManager.getConnection(url)) {
	        String query = "DELETE FROM competitions WHERE id = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setInt(1, competitionId);
	            pstmt.executeUpdate();
	            System.out.println("Competition deleted successfully.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error deleting competition: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	
	public static void main(String [] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("View Competitions");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(700, 600);
			frame.add(new ScoresBySeason());
			frame.setVisible(true);
		});
	}
}
