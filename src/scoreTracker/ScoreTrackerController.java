package scoreTracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ScoreTrackerController {

    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private int accountId = 0; 
    private static NetworkClient client;

    public void createAndShowGUI() {
        frame = new JFrame("Figure Skating Score Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Top bar with title and account link
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Figure Skating Score Tracker");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        topBar.add(title, BorderLayout.WEST);

        JLabel accountLink = new JLabel("<HTML><U>Account</U></HTML>");
        accountLink.setForeground(Color.BLUE);
        accountLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accountLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel, "Account");
            }
        });
        topBar.add(accountLink, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Nav bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navBar.add(createNavLabel("Compare Scores", "Compare"));
        navBar.add(createNavLabel("All Scores", "Season"));
        navBar.add(createNavLabel(" + New Competition", "NewComp"));
        navBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.LIGHT_GRAY));

        // Card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add screens
        cardPanel.add(createSkaterPanel(), "Account");
        //cardPanel.add(comparePanel(), "Compare");
        // Don't add the "Season" panel upfront, it will be created dynamically
        cardPanel.add(new JPanel(), "NewComp");

        // Layout setup
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(navBar, BorderLayout.AFTER_LAST_LINE);
        frame.add(cardPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JLabel createNavLabel(String text, String cardName) {
        JLabel label = new JLabel("<HTML><U>" + text + "</U></HTML>");
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked on: " + cardName);
                if (cardName.equals("Season")) {
                    // Dynamically create the Season panel when it's clicked
                    JPanel seasonPanel = scoresBySeason();
                    cardPanel.add(seasonPanel, "Season");
                }
                
                if (cardName.equals("Compare")) {
                	JPanel comparison = comparePanel();
                	cardPanel.add(comparison, "Compare");
                }
                cardLayout.show(cardPanel, cardName);
            }
        });
        return label;
    }

    private JPanel scoresBySeason() {
        System.out.println("Creating panel for Season...");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dynamically create the ScoresBySeason panel each time it is shown
        ScoresBySeason scoresBySeason = new ScoresBySeason(accountId);
        panel.add(scoresBySeason);

        return panel;
    }

    private JPanel createNewComp() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        System.out.println("accountId in createNewComp: " + accountId);
        if (accountId != 0) {
            ScoreCalculator scoreTablePanel = new ScoreCalculator(accountId);
            panel.add(scoreTablePanel);
        }

        return panel;
    }

    private JPanel comparePanel() {
    	System.out.println("CREATIng compare");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        CompareScores compareScores = new CompareScores(accountId);
        panel.add(compareScores);

        return panel;
    }

    private JPanel createSkaterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Login or Register");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(titleLabel);

        JTextField username = new JTextField();
        username.setMaximumSize(new Dimension(250, 5));
        JPasswordField password = new JPasswordField();
        password.setMaximumSize(new Dimension(250, 5));

        panel.add(new JLabel("Username: "));
        panel.add(username);
        panel.add(Box.createVerticalStrut(5));

        panel.add(new JLabel("Password: "));
        panel.add(password);
        panel.add(Box.createVerticalStrut(10));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        panel.add(buttonPanel);

        JLabel feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setForeground(Color.RED);
        panel.add(feedbackLabel);

        loginButton.addActionListener(e -> {
            String usernameField = username.getText();
            String passwordField = new String(password.getPassword());

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
                PreparedStatement stmt = conn.prepareStatement("SELECT id FROM accounts WHERE username = ? AND password = ?");
                stmt.setString(1, usernameField);
                stmt.setString(2, passwordField);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Store accountId when login is successful
                    ScoreTrackerController.this.accountId = rs.getInt("id");
                    System.out.println("account id after login: " + accountId);  // Debug print

                    feedbackLabel.setForeground(Color.GREEN);
                    feedbackLabel.setText("Login successful");
                    
                    JPanel newCompPanel = createNewComp();
                    cardPanel.add(newCompPanel, "NewComp");

                    // Ensure accountId is valid before showing "NewComp"
                    if (accountId != 0) {
                        // Now that accountId is set, transition to NewComp
                        cardLayout.show(cardPanel, "NewComp");
                    } else {
                        feedbackLabel.setText("Login failed. Please try again.");
                    }
                } else {
                    feedbackLabel.setForeground(Color.RED);
                    feedbackLabel.setText("Invalid credentials");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                feedbackLabel.setText("Database error");
            }
        });



        registerButton.addActionListener(e -> {
            String usernameField = username.getText();
            String passwordField = new String(password.getPassword());

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO accounts (username, password) VALUES (?, ?)");
                stmt.setString(1, usernameField);
                stmt.setString(2, passwordField);
                stmt.executeUpdate();

                // Get the newly created accountId
                ResultSet rs = conn.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    ScoreTrackerController.this.accountId = rs.getInt(1);
                }

                feedbackLabel.setForeground(Color.GREEN);
                feedbackLabel.setText("Account Created!");
                
                JPanel accountPanel = createSkaterPanel();
                cardPanel.add(accountPanel, "Account");

                Timer timer = new Timer(1500, event -> {
                    cardLayout.show(cardPanel, "Account");
                });
                timer.setRepeats(false);
                timer.start();

            } catch (SQLException ex) {
                if (ex.getMessage().contains("UNIQUE constraint failed")) {
                    feedbackLabel.setText("Username already exists");
                } else {
                    ex.printStackTrace();
                    feedbackLabel.setText("Registration failed");
                }
            }
        });

        return panel;
    }

    public static NetworkClient getNetworkClient() {
        return client;
    }

    public static void main(String[] args) {
        try {
        	ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "NetworkServer");
            //pb.inheritIO();
            pb.start();
            Thread.sleep(1000);
            client = new NetworkClient("localhost", 8000);
            System.out.println("Connected to server!");
        } catch (Exception e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new ScoreTrackerController().createAndShowGUI();
        });
    }
}



