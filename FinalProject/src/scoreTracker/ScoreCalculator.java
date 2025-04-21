package scoreTracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;

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

    public ScoreCalculator() {
        setLayout(new BorderLayout());
        

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
        add(radioPanel, BorderLayout.NORTH);

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

        updateScores();
        printDatabaseSchema();
    }

    public void updateScores() {
        if (isUpdating) return;
        isUpdating = true;

        DefaultTableModel tesModel = currentSelection.equals("Short") ? tesModelShort : tesModelFree;
        DefaultTableModel pcsModel = currentSelection.equals("Short") ? pcsModelShort : pcsModelFree;

        double[] tesScores = calculateTES(tesModel);
        double totalTES = tesScores[0];
        double totalPCS = calculatePCSTotal(pcsModel);
        double overallTotal = totalTES + totalPCS;

        totalScoreLabel.setText("Total TES Score: " + String.format("%.2f", totalTES));
        totalPCSLabel.setText("Total PCS Score: " + String.format("%.2f", totalPCS));
        overallTotalLabel.setText("Overall Total Score: " + String.format("%.2f", overallTotal));

        isUpdating = false;
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
                total += factor * mark;
            } catch (Exception e) {
                // ignore
            }
        }
        model.setValueAt("Total", model.getRowCount() - 1, 0);
        model.setValueAt("", model.getRowCount() - 1, 1);
        model.setValueAt(String.format("%.2f", total), model.getRowCount() - 1, 2);
        return total;
    }

    public void saveProgramScore(String type, double totalScore, int compeitionId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO programs (competition_id, type, total_score) VALUES (?,?,?)");
            pstmt.setInt(1, compeitionId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, totalScore);
            pstmt.executeUpdate();
            System.out.println(type + " score saved to DB");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCompetitionDetails(String newTitle, String date, int competitionId) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
            System.out.println("Saving competition: " + newTitle + " on " + date);
            PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE competitions SET title = ?, date = ? WHERE competition_id = ?");
            pstmt.setString(1, newTitle);
            pstmt.setString(2, date);
            pstmt.setInt(3, competitionId);
            pstmt.executeUpdate();
            System.out.println("Competition updated in DB: " + newTitle + " on " + date);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printDatabaseSchema() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:skating_scores.db")) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT sql FROM sqlite_master WHERE type='table'");
            var rs = pstmt.executeQuery();
            System.out.println("----- Current Database Schema -----");
            while (rs.next()) {
                String tableDef = rs.getString("sql");
                System.out.println(tableDef);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public double getShortScore() {
        double tes = calculateTES(tesModelShort)[0];
        double pcs = calculatePCSTotal(pcsModelShort);
        return tes + pcs;
    }

    public double getFreeScore() {
        double tes = calculateTES(tesModelFree)[0];
        double pcs = calculatePCSTotal(pcsModelFree);
        return tes + pcs;
    }

    public double getTotalScore() {
        return getShortScore() + getFreeScore();
    }
}


