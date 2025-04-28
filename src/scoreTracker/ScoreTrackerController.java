package scoreTracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ScoreTrackerController {

    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private int accountId;
    
    public void ScoreTrackerController(int accountId) {
    	this.accountId = accountId;
    }

    public void createAndShowGUI() {
        frame = new JFrame("Figure Skating Score Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Top title label
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Figure Skating Score Tracker");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        topBar.add(title, BorderLayout.WEST);
        
        JLabel accountLink = new JLabel("<HTML><U>Account</U></HTML>");
        accountLink.setForeground(Color.BLUE);
        accountLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accountLink.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
        		cardLayout.show(cardPanel,  "Account");
        	}
        });
        topBar.add(accountLink, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // --Nav Bar--
        JPanel navBar = new JPanel();
        navBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        navBar.add(createNavLabel("Compare Scores", "Compare"));
        navBar.add(createNavLabel("All Scores", "Season"));
        navBar.add(createNavLabel(" + New Competition", "NewComp"));
        
        navBar.setBorder(BorderFactory.createMatteBorder(1,0,1,0, Color.LIGHT_GRAY));
        

        // CardLayout for switching "screens"
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add screens
        cardPanel.add(createNewComp(), "Home");
        cardPanel.add(createSkaterPanel(), "Account");
        cardPanel.add(comparePanel(), "Compare");
        cardPanel.add(scoresBySeason(), "Season");
        cardPanel.add(createNewComp(), "NewComp");

        // Add the card panel to the frame
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(navBar, BorderLayout.AFTER_LAST_LINE);
        frame.add(cardPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
    
    private JLabel createNavLabel(String text, String cardName) {
    	JLabel label = new JLabel("<HTML><U>" + text + "</U></HTML>");
    	label.setForeground(Color.BLUE);
    	label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    	label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
    	label.addMouseListener(new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {
    			cardLayout.show(cardPanel, cardName);
    		}
    	});
    	return label;
    }

    private JPanel createNewComp() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Score Table Component
        ScoreCalculator scoreTablePanel = new ScoreCalculator();
        panel.add(scoreTablePanel);
        

        return panel;
    }
    
    
    private JPanel scoresBySeason() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        ScoresBySeason scoresBySeason = new ScoresBySeason();
        
        panel.add(scoresBySeason);
        return panel;
    }	
    
    private JPanel comparePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        CompareScores compareScores = new CompareScores();
        
        panel.add(compareScores);
        return panel;
    }
    
    private void saveCompetitionDetails(String newTitle, double shortScore, double freeScore, double totalScore) {
        String url = "jdbc:sqlite:skating_scores.db";
        String sql = "INSERT INTO competitions(account_id, name, short_score, free_score, total_score) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.setString(2, newTitle);
            pstmt.setDouble(3, shortScore);
            pstmt.setDouble(4, freeScore);
            pstmt.setDouble(5, totalScore);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Competition saved successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving competition: " + e.getMessage());
        }
    }


    private JPanel createSkaterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Account Page - coming soon", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JButton backButton = new JButton("Back to Calculator");
        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Home");
        });

        panel.add(label, BorderLayout.CENTER);
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ScoreTrackerController().createAndShowGUI();
        });
    }
    

}


