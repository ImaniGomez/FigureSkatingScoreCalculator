package scoreTracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

public class ScoreCalculator extends JPanel {

    private JRadioButton compShort, compFree;
    private String currentSelection = "Short";

    private DefaultTableModel tesModelShort, tesModelFree;
    private JTable tesTableShort, tesTableFree;
    private JLabel totalScoreLabel;
    private DefaultTableModel pcsModelShort, pcsModelFree;
    private JTable pcsTableShort, pcsTableFree;
    private JLabel totalPCSLabel;
    private JLabel overallTotalLabel;

    private boolean isUpdating = false;
    private JPanel tableContainer;
    private CardLayout tableSwitcher;
    
    private double shortProgramTES = 0.0;
    private double shortProgramPCS = 0.0;
    private double freeProgramTES = 0.0;
    private double freeProgramPCS = 0.0;
    
    private int accountId;

    public ScoreCalculator(int accountId) {
    	this.accountId = accountId;
    	System.out.println("3");
    	//this.accountId = fetchAccountId();
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Title Text Field
        JTextField titleField = new JTextField("New Competition"); // Make this an instance variable if needed
        titleField.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleField.getPreferredSize().height));
        topPanel.add(titleField);
        topPanel.add(Box.createVerticalStrut(10)); // Add spacing


        // Radio Buttons
        compShort = new JRadioButton("Short");
        compFree = new JRadioButton("Free");
        compShort.setSelected(true);
        ButtonGroup compSelection = new ButtonGroup();
        compSelection.add(compShort);
        compSelection.add(compFree);

        JPanel radioPanel = new JPanel();
        radioPanel.add(new JLabel("Program: "));
        radioPanel.add(compShort);
        radioPanel.add(compFree);
        topPanel.add(radioPanel); // Add radio buttons to the same top section

        // Now add the whole top section to the ScoreCalculator
        add(topPanel, BorderLayout.NORTH);

        JTextField dateField = new JTextField("2025-04-14", 10);
        radioPanel.add(new JLabel("Date:"));
        radioPanel.add(dateField);

        // TES Table
        String[] tesColumnNames = {"Element", "BV", "GOE", "Total"};
        tesModelShort = new DefaultTableModel(tesColumnNames, 0);
        tesModelFree = new DefaultTableModel(tesColumnNames, 0);

        tesTableShort = new JTable(tesModelShort);
        tesTableFree = new JTable(tesModelFree);

        for (int i = 0; i < 3; i++) tesModelShort.addRow(new Object[]{"", 0.0, 0.0, 0.0});
        tesModelShort.addRow(new Object[]{"Total", 0.0, 0.0, 0.0});

        for (int i = 0; i < 3; i++) tesModelFree.addRow(new Object[]{"", 0.0, 0.0, 0.0});
        tesModelFree.addRow(new Object[]{"Total", 0.0, 0.0, 0.0});

        // PCS Table
        String[] pcsColumnNames = {"Component", "Factor", "PCS Mark"};
        pcsModelShort = new DefaultTableModel(pcsColumnNames, 0);
        pcsModelFree = new DefaultTableModel(pcsColumnNames, 0);

        pcsTableShort = new JTable(pcsModelShort);
        pcsTableFree = new JTable(pcsModelFree);

        pcsModelShort.addRow(new Object[]{"Composition", 1.0, 0.0});
        pcsModelShort.addRow(new Object[]{"Presentation", 1.0, 0.0});
        pcsModelShort.addRow(new Object[]{"Skating Skills", 1.0, 0.0});
        pcsModelShort.addRow(new Object[]{"Total", "", 0.0});

        pcsModelFree.addRow(new Object[]{"Composition", 1.0, 0.0});
        pcsModelFree.addRow(new Object[]{"Presentation", 1.0, 0.0});
        pcsModelFree.addRow(new Object[]{"Skating Skills", 1.0, 0.0});
        pcsModelFree.addRow(new Object[]{"Total", "", 0.0});

        // Add Row Button
        JButton addRowButton = new JButton("Add Row to TES");
        addRowButton.addActionListener(e -> {
            DefaultTableModel tesModel = currentSelection.equals("Short") ? tesModelShort : tesModelFree;
            tesModel.insertRow(tesModel.getRowCount() - 1, new Object[]{"", 0.0, 0.0, 0.0});
        });

        // Table Switching
        tableSwitcher = new CardLayout();
        tableContainer = new JPanel(tableSwitcher);

        JPanel shortPanel = new JPanel(new GridLayout(2, 1));
        shortPanel.add(new JScrollPane(tesTableShort));
        shortPanel.add(new JScrollPane(pcsTableShort));

        JPanel freePanel = new JPanel(new GridLayout(2, 1));
        freePanel.add(new JScrollPane(tesTableFree));
        freePanel.add(new JScrollPane(pcsTableFree));

        tableContainer.add(shortPanel, "Short");
        tableContainer.add(freePanel, "Free");

        add(tableContainer, BorderLayout.CENTER);

        // Score Labels
        radioPanel.add(addRowButton);
        totalScoreLabel = new JLabel("Total TES Score: 0.00");
        totalPCSLabel = new JLabel("Total PCS Score: 0.00");
        overallTotalLabel = new JLabel("Overall Total Score: 0.00");

        JPanel scoresPanel = new JPanel();
        scoresPanel.setLayout(new GridLayout(5, 1));
        scoresPanel.add(totalScoreLabel);
        scoresPanel.add(totalPCSLabel, BorderLayout.CENTER);
        scoresPanel.add(overallTotalLabel, BorderLayout.EAST);

        add(scoresPanel, BorderLayout.SOUTH);

        // Listeners
        tesModelShort.addTableModelListener(e -> updateScores());
        tesModelFree.addTableModelListener(e -> updateScores());
        pcsModelShort.addTableModelListener(e -> updateScores());
        pcsModelFree.addTableModelListener(e -> updateScores());

        compShort.addActionListener(e -> {
            currentSelection = "Short";
            tableSwitcher.show(tableContainer, "Short");
            updateScores();
        });

        compFree.addActionListener(e -> {
            currentSelection = "Free";
            tableSwitcher.show(tableContainer, "Free");
            updateScores();
        });
        
        JButton saveButton = new JButton("Save Competition");

        saveButton.addActionListener(e -> {
            try {
                String newTitle = titleField.getText();
                
                double shortScore = calculateTES(tesModelShort)[0] + calculatePCSTotal(pcsModelShort);
                double freeScore = calculateTES(tesModelFree)[0] + calculatePCSTotal(pcsModelFree);
                double totalScore = shortScore + freeScore;
                
                System.out.println("Account id from calc: " + accountId);

                // Check if accountId is valid (non-zero)
                if (accountId == 0) {
                    JOptionPane.showMessageDialog(ScoreCalculator.this, "Must log in to save competitions", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save the competition with the valid accountId
                saveCompetition(newTitle, dateField.getText(), shortScore, freeScore, totalScore, accountId);

                JOptionPane.showMessageDialog(ScoreCalculator.this, "Competition saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                
             // --- RESET UI ---

                // Reset Short TES table
                tesModelShort.setRowCount(0);
                for (int i = 0; i < 3; i++) tesModelShort.addRow(new Object[]{"", 0.0, 0.0, 0.0});
                tesModelShort.addRow(new Object[]{"Total", 0.0, 0.0, 0.0});

                // Reset Free TES table
                tesModelFree.setRowCount(0);
                for (int i = 0; i < 3; i++) tesModelFree.addRow(new Object[]{"", 0.0, 0.0, 0.0});
                tesModelFree.addRow(new Object[]{"Total", 0.0, 0.0, 0.0});

                // Reset PCS Short
                pcsModelShort.setRowCount(0);
                pcsModelShort.addRow(new Object[]{"Composition", 1.0, 0.0});
                pcsModelShort.addRow(new Object[]{"Presentation", 1.0, 0.0});
                pcsModelShort.addRow(new Object[]{"Skating Skills", 1.0, 0.0});
                pcsModelShort.addRow(new Object[]{"Total", "", 0.0});

                // Reset PCS Free
                pcsModelFree.setRowCount(0);
                pcsModelFree.addRow(new Object[]{"Composition", 1.0, 0.0});
                pcsModelFree.addRow(new Object[]{"Presentation", 1.0, 0.0});
                pcsModelFree.addRow(new Object[]{"Skating Skills", 1.0, 0.0});
                pcsModelFree.addRow(new Object[]{"Total", "", 0.0});

                // Reset text fields
                titleField.setText("New Competition");
                dateField.setText("2025-04-14");

                // Reset score variables
                shortProgramTES = 0.0;
                shortProgramPCS = 0.0;
                freeProgramTES = 0.0;
                freeProgramPCS = 0.0;

                updateScores();
                

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        
        scoresPanel.add(saveButton);


        updateScores();
        printDatabaseSchema();
    }
    

    public void updateScores() {
        if (isUpdating) return;
        isUpdating = true;

        SwingWorker<Void, Void> scoreWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DefaultTableModel tesModel = currentSelection.equals("Short") ? tesModelShort : tesModelFree;
                DefaultTableModel pcsModel = currentSelection.equals("Short") ? pcsModelShort : pcsModelFree;

                double[] tesScores = calculateTES(tesModel);
                double totalTES = tesScores[0];
                double totalPCS = calculatePCSTotal(pcsModel);

                // Perform calculations for short or free program based on selection
                if (currentSelection.equals("Short")) {
                    shortProgramTES = totalTES;
                    shortProgramPCS = totalPCS;
                } else {
                    freeProgramTES = totalTES;
                    freeProgramPCS = totalPCS;
                }

                double overallTotal = (shortProgramTES + shortProgramPCS) + (freeProgramTES + freeProgramPCS);

                // Update the UI after calculations
                SwingUtilities.invokeLater(() -> {
                    totalScoreLabel.setText("Total TES Score: " + String.format("%.2f", totalTES));
                    totalPCSLabel.setText("Total PCS Score: " + String.format("%.2f", totalPCS));
                    overallTotalLabel.setText("Overall Total Score: " + String.format("%.2f", overallTotal));
                });

                isUpdating = false;
                return null;
            }
        };

        scoreWorker.execute();
    }


    private double[] calculateTES(DefaultTableModel model) {
        double total = 0.0, bv = 0.0, goe = 0.0;
        for (int row = 0; row < model.getRowCount() - 1; row++) {
            try {
                double bvVal = Double.parseDouble(model.getValueAt(row, 1).toString());
                double goeVal = Double.parseDouble(model.getValueAt(row, 2).toString());
                
                
                model.setValueAt(String.format("%.2f", bvVal + goeVal), row, 3);
                bv += bvVal;
                goe += goeVal;
                total += bvVal + goeVal;
            } catch (Exception e) {
                model.setValueAt("", row, 3);
            }
        }
        model.setValueAt("Total", model.getRowCount() - 1, 0);
        model.setValueAt(String.format("%.2f", bv), model.getRowCount() - 1, 1);
        model.setValueAt(String.format("%.2f", goe), model.getRowCount() - 1, 2);
        model.setValueAt(String.format("%.2f", total), model.getRowCount() - 1, 3);
        return new double[]{total, bv, goe};
    }

    private double calculatePCSTotal(DefaultTableModel model) {
        double total = 0.0;
        for (int row = 0; row < model.getRowCount() - 1; row++) {
            try {
                double factor = Double.parseDouble(model.getValueAt(row, 1).toString());
                double mark = Double.parseDouble(model.getValueAt(row, 2).toString());
                total += (factor * mark);
                System.out.print(total);
            } catch (Exception e) {
                // ignore
            }
        }
        model.setValueAt("Total", model.getRowCount() - 1, 0);
        model.setValueAt("", model.getRowCount() - 1, 1);
        model.setValueAt(String.format("%.2f", total), model.getRowCount() - 1, 2);
        return total;
    }

    
    public void saveCompetition(String title, String date, double shortScore, double freeScore, double totalScore, int account_id) {
    	System.out.println("Starting Save");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
            conn.setAutoCommit(false);
            System.out.println("Saving with accountId: " + accountId);

            // 1. Insert competition and get its ID
            PreparedStatement insertComp = conn.prepareStatement(
                "INSERT INTO competitions (name, date, short_score, free_score, total_score, account_id) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            insertComp.setString(1, title);
            insertComp.setString(2, date);
            insertComp.setDouble(3, shortScore);
            insertComp.setDouble(4,  freeScore);
            insertComp.setDouble(5, totalScore);
            insertComp.setInt(6,  account_id);
            insertComp.executeUpdate();

            ResultSet keys = insertComp.getGeneratedKeys();
            int competitionId = -1;
            if (keys.next()) {
                competitionId = keys.getInt(1);
            } else {
                conn.rollback();
                throw new SQLException("Failed to retrieve competition ID.");
            }

            // 2. Insert short program
            PreparedStatement shortStmt = conn.prepareStatement(
                "INSERT INTO programs (competition_id, type, total_score) VALUES (?, ?, ?)", 
                Statement.RETURN_GENERATED_KEYS);
            shortStmt.setInt(1, competitionId);
            shortStmt.setString(2, "short");
            shortStmt.setDouble(3, shortScore);
            shortStmt.executeUpdate();
            ResultSet shortKeys = shortStmt.getGeneratedKeys();
            int shortId = shortKeys.next() ? shortKeys.getInt(1) : -1;

            // 3. Insert free program
            PreparedStatement freeStmt = conn.prepareStatement(
                "INSERT INTO programs (competition_id, type, total_score) VALUES (?, ?, ?)");
            freeStmt.setInt(1, competitionId);
            freeStmt.setString(2, "free");
            freeStmt.setDouble(3, freeScore);
            freeStmt.executeUpdate();
            ResultSet freeKeys = freeStmt.getGeneratedKeys();
            int freeId = freeKeys.next() ? freeKeys.getInt(1) : -1;
            
            saveTESAndPCSData(conn, shortId, tesModelShort, pcsModelShort);
            saveTESAndPCSData(conn, freeId, tesModelFree, pcsModelFree);

            conn.commit();
            System.out.println("Full competition saved successfully.");
            
            
            
            

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    private void saveTESAndPCSData(Connection conn, int programId, DefaultTableModel tesModel, DefaultTableModel pcsModel) throws SQLException {
    	PreparedStatement insertElement = conn.prepareStatement(
    			"INSERT INTO elements (program_id, element, bv, goe, total) VALUES (?, ?, ?, ?, ?)"
    	);
    	
    	for (int i = 0; i < tesModel.getRowCount() - 1; i++) {
    		insertElement.setInt(1,  programId);
    		insertElement.setString(2,  tesModel.getValueAt(i,  0).toString());
    		insertElement.setDouble(3, Double.parseDouble(tesModel.getValueAt(i,  1).toString()));
    		insertElement.setDouble(4, Double.parseDouble(tesModel.getValueAt(i, 2).toString()));
    		insertElement.setDouble(5,  Double.parseDouble(tesModel.getValueAt(i,  3).toString()));
    		insertElement.executeUpdate();
    		
    	}
    	
    	PreparedStatement insertComponent = conn.prepareStatement(
    			"INSERT INTO components (program_id, component, factor, mark) VALUES (?, ?, ?, ?)"
    	);
    	
    	for (int i = 0; i < pcsModel.getRowCount() - 1; i++) {
    		insertComponent.setInt(1, programId);
    		insertComponent.setString(2,  pcsModel.getValueAt(i,  0).toString());
    		insertComponent.setDouble(3,  Double.parseDouble(pcsModel.getValueAt(i,  1).toString()));
    		insertComponent.setDouble(4,  Double.parseDouble(pcsModel.getValueAt(i,  2).toString()));
    		insertComponent.executeUpdate();
    	}
    			
    }


    public void printDatabaseSchema() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT sql FROM sqlite_master WHERE type='table'");
            var rs = pstmt.executeQuery();
            
            System.out.println("----- Starting Calculator -----");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    } 
    


}


