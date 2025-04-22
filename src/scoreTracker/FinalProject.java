package scoreTracker;
import javax.swing.SwingUtilities;

public class FinalProject {
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure UI creation happens on the EDT
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create and show the UI
                ScoreTrackerController simpleUI = new ScoreTrackerController();
                simpleUI.createAndShowGUI();
            }
        });
    }
}
