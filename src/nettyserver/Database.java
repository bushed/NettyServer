package nettyserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to DB.
 * @author Gribakin O
 */
public final class Database {

    public static final String DRIVER = "org.hsqldb.jdbcDriver";
    public static final String JDBC_URL = "jdbc:hsqldb:file:localdb/db;create=true";
    private static Connection connection;
    
    static {
        initConnection();
    }
    
    /**
     * initiate driver and connection to database
     */
    static void initConnection(){
        try {
            Class.forName(DRIVER).newInstance();
            connection = DriverManager.getConnection(JDBC_URL); 
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, "Driver initialization error", ex);
        }
        StatDAO stat = new StatDAO();
        stat.create();
    }

    /**
     * 
     * @return connection
     * @throws SQLException
     */
    public static synchronized Connection getConnection() throws SQLException {
        return connection;      
    }
    
    /**
     * Close connection to database. 
     * @throws SQLException
     */
    public static void closeConnection() throws SQLException{
        if (connection != null && !connection.isClosed()) {
            connection.close();            
        }        
    }
}



