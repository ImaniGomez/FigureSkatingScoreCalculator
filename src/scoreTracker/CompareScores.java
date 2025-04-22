package scoreTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CompareScores extends JPanel{
	private JComboBox<String> comboBox1 = new JComboBox<>();
	private JComboBox<String> comboBox2 = new JComboBox<>();
	
	private JPanel scorePanel1 = new JPanel(new BorderLayout());
	private JPanel scorePanel2 = new JPanel(new BorderLayout());
	
	private HashMap<String, Integer> compNameId = new HashMap<>();
	
	private String db = "jdbc:sqlite:skating_scores.db";
	private int accountId = 1;
	
	public CompareScores() {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(20, 20, 20, 20));
		
		loadCompetitions();
		
		JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		topPanel.add(sidePanel(comboBox1, scorePanel1, true));
		topPanel.add(sidePanel(comboBox2, scorePanel2, false));
		
		add(topPanel, BorderLayout.CENTER);
	}
	
	private JPanel sidePanel(JComboBox<String> box, JPanel scorePanel, boolean isLeft) {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createTitledBorder(isLeft ? "Compare A" : "Compare B"));
		
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(box, BorderLayout.NORTH);
		
		JPanel togglePanel = new JPanel();
		ButtonGroup toggleGroup = new ButtonGroup();
		JRadioButton shortBtn = new JRadioButton("Short Program");
		JRadioButton freeBtn = new JRadioButton ("Free Program");
		
		toggleGroup.add(shortBtn);
		toggleGroup.add(freeBtn);
		togglePanel.add(shortBtn);
		togglePanel.add(freeBtn);
		
		selectionPanel.add(togglePanel, BorderLayout.SOUTH);
		
		panel.add(selectionPanel, BorderLayout.NORTH);
		panel.add(scorePanel, BorderLayout.CENTER);
		
		box.addActionListener(e -> updateScores(box, scorePanel, shortBtn.isSelected() ? "short" : "long"));
		shortBtn.addActionListener(e -> updateScores(box, scorePanel, "short"));
		freeBtn.addActionListener(e -> updateScores(box, scorePanel, "free"));
		
		shortBtn.setSelected(true);
		
		return panel;
	}
	
	private void loadCompetitions() {
		try (Connection conn = DriverManager.getConnection(db)){
			String query = """
					SELECT 
						c.id AS competition_id, 
						c.name AS competition_name
					FROM competitions c
					WHERE c.account_id = ?;
			""";

			
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1,  accountId);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				int id = rs.getInt("competition_id");
				String name = rs.getString("competition_name");
				comboBox1.addItem(name);
				comboBox2.addItem(name);
				compNameId.put(name,  id);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateScores(JComboBox<String> box, JPanel scorePanel, String programType) {
		scorePanel.removeAll();
		String selected = (String) box.getSelectedItem();
		
		if (selected == null) {
			return;
		}
		
		int compId = compNameId.get(selected);
		
		try(Connection conn = DriverManager.getConnection(db)){
			PreparedStatement stmt = conn.prepareStatement("""
					SELECT total_score FROM programs
					WHERE competition_id = ? AND type = ?
				""");
			
			stmt.setInt(1, compId);
			stmt.setString(2,  programType);
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				double score = rs.getDouble("total_score");
				JLabel label = new JLabel(programType.substring(0, 1).toUpperCase() + programType.substring(1) + "Score: " + score);
				scorePanel.add(label, BorderLayout.CENTER);
			}else {
				scorePanel.add(new JLabel("No score found"), BorderLayout.CENTER);
			}

		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		scorePanel.revalidate();
		scorePanel.repaint();
	}
	
	public static void main(String [] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Compare Scores");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 400);
			frame.setContentPane(new CompareScores());
			frame.setVisible(true);
		});
	}
	
}
