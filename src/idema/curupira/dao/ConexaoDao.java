package idema.curupira.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexaoDao {

    private static Connection instance = null;

    private ConexaoDao() {

        // Create a variable for the connection string.
        //String connectionUrl = "jdbc:postgresql://localhost:5432/bdcurupira;" + "databaseName=bdcurupira"; 
        try {
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConexaoDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            //instance = DriverManager.getConnection(connectionUrl, "idema", "pr0t3c@0");
            instance = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bdcurupira", "idema", "pr0t3c@0");
        } catch (SQLException ex) {
            Logger.getLogger(ConexaoDao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            new ConexaoDao();
        }
        return instance;
    }
}
