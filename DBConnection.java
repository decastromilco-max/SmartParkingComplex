package smartparkinng;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection connect() {
        try {
            String url = "jdbc:sqlite:smartparking.db";
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
    }
}

