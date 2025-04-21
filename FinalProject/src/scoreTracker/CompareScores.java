package scoreTracker;

import java.sql.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CompareScores extends JPanel{
	public CompareScores() {
		setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		add(scrollPane, BorderLayout.CENTER);
		
		loadCompetitions(contentPanel);
		
	}
	
	private void loadCompetitions(JPanel contentPanel) {
		String url = "jdbc:sqlite:skating_scores.db";
		
		try(Connection conn = DriverManager.getConnection(url)){
			String query = """
				SELECT 
					c.id AS competition_id, 
					c.name AS competition_name,
					c.date AS competition_date,
					sp.total_score AS short_program_score, 
					lp.total_score AS free_program_score, 
					(sp.total_score + lp.total_score) AS total_score
				FROM competitions c
				LEFT JOIN programs sp ON c.id = sp.competition_id AND sp.type = 'short'
				LEFT JOIN programs lp ON c.id = lp.competition_id AND lp.type = 'long'
				WHERE c.account_id = ?;
			""";
			
			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int compId = rs.getInt("competition_id");
				String competitionName = rs.getString("Competition_name");
				double shortProgramScore = rs.getDouble("short_program_score");
				double freeProgramScore = rs.getDouble("free_program_score");
				double totalScore = rs.getDouble("total_score");
				
				JPanel compPanel = new JPanel(new BorderLayout());
				compPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				compPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
				
				JLabel compLabel = new JLabel(String.format("&s: Short=%.2f, Free=%.2f, Total=%.2f", competitionName, shortProgramScore, freeProgramScore, totalScore));
				
				compLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
				
				
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
