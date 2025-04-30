package scoreTracker;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//handles database management
public class DatabaseManager {
	public static void main(String [] args) {
		String dbPath = "skating_scores.db";
		String url = "jdbc:sqlite:" + dbPath;
		
		try (Connection conn = DriverManager.getConnection(url)){
			if(conn != null) {
				System.out.println("Database created at: " + dbPath);
				try(Statement stmt = conn.createStatement()){

					stmt.execute("""
						CREATE TABLE IF NOT EXISTS accounts(
							id INTEGER PRIMARY KEY AUTOINCREMENT, 
							username TEXT UNIQUE, 
							password TEXT, 
							logged_in BOOLEAN DEFAULT 0
						);
					""");
					
					stmt.execute("""
							CREATE TABLE IF NOT EXISTS competitions (
								id INTEGER PRIMARY KEY AUTOINCREMENT, 
								account_id INTEGER,
								name TEXT,
								date TEXT, 
								short_score REAL,
								free_score REAL, 
								total_score REAL,
								FOREIGN KEY (account_id) REFERENCES accounts(id)
							);
						""");
					stmt.execute("""
						CREATE TABLE IF NOT EXISTS programs (
							id INTEGER PRIMARY KEY AUTOINCREMENT, 
							competition_id INTEGER,
							type TEXT, -- 'short' or 'long'
							total_score REAL, 
							FOREIGN KEY (competition_id) REFERENCES competitions(id)
						);
						"""							
					);
					stmt.execute("""
						CREATE TABLE IF NOT EXISTS elements (
							id INTEGER PRIMARY KEY AUTOINCREMENT, 
							program_id INTEGER NOT NULL, 
							element TEXT, 
							bv REAL, 
							goe REAL, 
							total REAL, 
							FOREIGN KEY (program_id) REFERENCES programs(id)
							);
						"""
					);
					stmt.execute("""
							CREATE TABLE IF NOT EXISTS components (
								id INTEGER PRIMARY KEY AUTOINCREMENT,
								program_id INTEGER NOT NULL,
								component TEXT, 
								factor REAL, 
								mark REAL, 
								FOREIGN KEY (program_id) REFERENCES programs(id)
							);
						"""
					);

					System.out.println("Tables created successfully");
					System.out.println("SELECT * FROM accounts");
				}
			}
			
			
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Databse created failed: " + e.getMessage());
		}
	}
}
