package scoreTracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class CompareScores extends JPanel {
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
		panel.setBorder(BorderFactory.createTitledBorder(isLeft ? " " : "  "));

		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(box, BorderLayout.NORTH);

		JPanel togglePanel = new JPanel();
		ButtonGroup toggleGroup = new ButtonGroup();
		JRadioButton shortBtn = new JRadioButton("Short Program");
		JRadioButton freeBtn = new JRadioButton("Free Program");

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

		String url = "jdbc:sqlite:skating_scores.db";
		try (Connection conn = DriverManager.getConnection(url)) {
			String query = """
						SELECT id AS competition_id, name AS competition_name, date AS competition_date, short_score, free_score, total_score
						FROM competitions
					""";

			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("competition_id");
				String name = rs.getString("competition_name");
				String competitionDate = rs.getString("competition_date");
				double shortProgramScore = rs.getDouble("short_score");
                double longProgramScore = rs.getDouble("free_score");
                double totalScore = rs.getDouble("total_score");
				
				JPanel compPanel = new JPanel();
				compPanel.setLayout(new BoxLayout(compPanel, BoxLayout.Y_AXIS));
				compPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				
				// Add to both dropdowns
				comboBox1.addItem(name);
				comboBox2.addItem(name);
				compNameId.put(name, id);
			}
		} catch (SQLException e) {
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
             totalScore += total;
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

	private void updateScores(JComboBox<String> box, JPanel scorePanel, String programType) {
		scorePanel.removeAll();
		String selected = (String) box.getSelectedItem();

		if (selected == null)
			return;

		int compId = compNameId.get(selected);
		
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		
		// Add the TES panel
	    JScrollPane tesPanel = getTESPanel(compId, programType);
	    innerPanel.add(tesPanel);
	    
	    // Add a small space between TES and PCS
	    innerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	    
	    // Add the PCS panel
	    JScrollPane pcsPanel = getPCSPanel(compId, programType);
	    innerPanel.add(pcsPanel);
	    
	    scorePanel.add(innerPanel, BorderLayout.CENTER);

		scorePanel.revalidate();
		scorePanel.repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Compare Scores");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 400);
			frame.setContentPane(new CompareScores());
			frame.setVisible(true);
		});
	}
}
