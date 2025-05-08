package scoreTracker;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ScoresBySeason extends JPanel {
	
	private int accountId;

    public ScoresBySeason(int accountId) {
    	this.accountId = accountId;
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
        System.out.println("here");
        
        loadAllCompetitions(contentPanel);

    }

    private void loadAllCompetitions(JPanel panel) {
    	System.out.println("entering load all comps");
        String url = "jdbc:sqlite:skating_scores.db";

        try (Connection conn = DriverManager.getConnection(url)){
        	System.out.println("Database connection successful.");
        	String query = "SELECT id, name, date, short_score, free_score, total_score FROM competitions WHERE account_id=?";
        	PreparedStatement pstmt = conn.prepareStatement(query);
        	pstmt.setInt(1,  accountId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Account ID from season: " + accountId);
            
            while (rs.next()) {
                int compId = rs.getInt("id");
                String compName = rs.getString("name");
                String competitionDate = rs.getString("date");
                double shortProgramScore = rs.getDouble("short_score");
                double longProgramScore = rs.getDouble("free_score");
                double totalScore = rs.getDouble("total_score");
                
                JPanel compPanel = new JPanel();
                compPanel.setLayout(new BoxLayout(compPanel, BoxLayout.Y_AXIS));
                compPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                JLabel compLabel = new JLabel(String.format(
                	    "<html><b>%s</b> | %s | Short=%.2f, Free=%.2f, Total=%.2f</html>",
                	    compName, competitionDate, shortProgramScore, longProgramScore, totalScore
                	));
                compLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

                JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                headerPanel.add(compLabel);

                // Delete button for each competition
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.addActionListener(e -> {
                    // Use compId directly here to declare the competition
                    deleteCompetition(compId, panel, compPanel); // Call delete method with the compId
                    headerPanel.revalidate();
                    headerPanel.repaint();
                });
                
                //Create the toggle button for details
                JButton toggleBtn = new JButton("▼");

                // Top panel with dropdown and delete button
               
                headerPanel.add(toggleBtn, BorderLayout.EAST);  // Add toggle button here
                headerPanel.add(deleteBtn, BorderLayout.EAST);  // Add delete button here



                // Panel for radio buttons (Short, Free)
                
                JPanel detailsWrapper = new JPanel();
                detailsWrapper.setLayout(new BoxLayout(detailsWrapper, BoxLayout.Y_AXIS));
                detailsWrapper.setVisible(false);
                
                
                JRadioButton shortBtn = new JRadioButton("Short");
                shortBtn.setSelected(true);
                shortBtn.setMargin(new Insets(0,0,0,0));
                
                JRadioButton freeBtn = new JRadioButton("Free");
                freeBtn.setMargin(new Insets(0,0,0,0));
                
                ButtonGroup group = new ButtonGroup();
                group.add(shortBtn);
                group.add(freeBtn);
               
                
                
                JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                radioPanel.add(shortBtn);
                radioPanel.add(freeBtn);
                radioPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                radioPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
                
                

                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
                detailsPanel.setVisible(false);
                
                detailsWrapper.add(radioPanel);
                detailsWrapper.add(detailsPanel);
                
                
                
                compPanel.add(headerPanel, BorderLayout.NORTH);
                compPanel.add(detailsWrapper);
                compPanel.add(detailsPanel, BorderLayout.SOUTH);
                
                panel.add(compPanel);
                panel.revalidate();
                panel.repaint();
                

                toggleBtn.addActionListener(e -> {
                	//compPanel.add(radioPanel, BorderLayout.CENTER);
                	
                	boolean isVisible = detailsPanel.isVisible();
                	boolean isVisible2 = !detailsWrapper.isVisible();
                	
                	detailsWrapper.setVisible(isVisible2);
                	detailsPanel.setVisible(!detailsPanel.isVisible());
                	detailsPanel.removeAll();

                	  // Add radio buttons
                    toggleBtn.setText(isVisible ? "▼" : "▲");
                    
                    
                    String selectedProgramType = shortBtn.isSelected() ? "short" : "free";
                    detailsPanel.add(getTESPanel(compId, selectedProgramType));
                    detailsPanel.add(Box.createVerticalStrut(10)); // spacing
                    detailsPanel.add(getPCSPanel(compId, selectedProgramType));
                    
                    compPanel.revalidate();
                    compPanel.repaint();
                    panel.revalidate();
                    panel.repaint();

                });
                

                // Handle radio button actions
                shortBtn.addActionListener(e -> {
                    detailsPanel.removeAll();
                    detailsPanel.add(getTESPanel(compId, "short"));
                    detailsPanel.add(Box.createVerticalStrut(10)); // spacing
                    detailsPanel.add(getPCSPanel(compId, "short"));
                    detailsPanel.revalidate();
                    detailsPanel.repaint();
                });

                freeBtn.addActionListener(e -> {
                    detailsPanel.removeAll();
                    detailsPanel.add(getTESPanel(compId, "free"));
                    detailsPanel.add(Box.createVerticalStrut(10)); // spacing
                    detailsPanel.add(getPCSPanel(compId, "free"));
                    detailsPanel.revalidate();
                    detailsPanel.repaint();
                });

                
            }
  

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCompetition(int competitionId, JPanel panel, JPanel compPanel) {
        String url = "jdbc:sqlite:skating_scores.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            String query = "DELETE FROM competitions WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, competitionId);
                pstmt.executeUpdate();
                System.out.println("Competition deleted successfully.");
                panel.remove(compPanel);  // Remove the specific competition panel
                panel.revalidate();
                panel.repaint();
            }
        } catch (SQLException e) {
            System.err.println("Error deleting competition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JScrollPane getTESPanel(int competitionId, String programType) {
        String query = """
            SELECT sp.element, sp.bv, sp.goe, sp.total
            FROM elements sp
            WHERE sp.program_id IN (
                SELECT id FROM programs WHERE competition_id = ? AND type = ?
            )
        """;

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Element");
        model.addColumn("BV");
        model.addColumn("GOE");
        model.addColumn("Total");
        
        double totalScore = 0.0;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, competitionId);
            pstmt.setString(2,  programType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
            	 double total = rs.getDouble("total");
                 totalScore += total;
                 model.addRow(new Object[]{
                        rs.getString("element"),
                        rs.getDouble("bv"),
                        rs.getDouble("goe"),
                        rs.getDouble("total")
                });
            }
            

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JLabel label = new JLabel("Technical Elements (TES)"+ " | Total: " + String.format("%.2f", totalScore));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        

        JPanel wrapper = new JPanel(new BorderLayout());
        //wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(null);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JScrollPane scrollWrapper = new JScrollPane(wrapper);
        scrollWrapper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollWrapper;
    }

    private JScrollPane getPCSPanel(int competitionId, String programType) {
        String query = """
            SELECT cp.component, cp.factor, cp.mark
            FROM components cp
            WHERE cp.program_id IN (
                SELECT id FROM programs WHERE competition_id = ? AND type = ?
            )
        """;

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Component");
        model.addColumn("Factor");
        model.addColumn("Mark");

        double totalScore = 0.0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, competitionId);
            pstmt.setString(2,  programType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
           	 double total = rs.getDouble("mark");
           	 double factor = rs.getDouble("factor");
             totalScore += (total * factor);
                model.addRow(new Object[]{
                        rs.getString("component"),
                        rs.getDouble("factor"),
                        rs.getDouble("mark")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JLabel label = new JLabel("Program Components (PCS)"+ " | Total: " + String.format("%.2f", totalScore));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel wrapper = new JPanel(new BorderLayout());
        //wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(null);
        wrapper.add(label, BorderLayout.NORTH);
        //wrapper.add(label);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JScrollPane scrollWrapper = new JScrollPane(wrapper);
        scrollWrapper.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return scrollWrapper;
    }

    private class CompLoader extends SwingWorker<Void, Void> {
        private JPanel panel;

        public CompLoader(JPanel panel) {
            this.panel = panel;
        }

        @Override
        protected Void doInBackground() {
            loadAllCompetitions(panel);
            return null;
        }

        @Override
        protected void done() {
            revalidate();
            repaint();
        }
    }
/*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             JFrame frame = new JFrame("Scores by Season");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(new ScoresBySeason(accountId));
            frame.setVisible(true);
        });
    }
    */
}

