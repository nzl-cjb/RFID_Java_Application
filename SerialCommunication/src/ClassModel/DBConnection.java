/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClassModel;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Callum
 */
public class DBConnection {
    
    private Connection connection;
    
    /**
     * Default constructor for DBConnection class.
     * The constructor initializes the connection variable for the class
     */
    public DBConnection() {
        try {
            String dbName = "rfid";
            String dbUsername = "root";
            String dbPassword = "";

            String dbDriver = "com.mysql.jdbc.Driver";
            Class.forName(dbDriver);

            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/"+dbName, dbUsername, dbPassword);
        } catch (Exception ex) {
        }
    }
    
    /**
     * This method returns the connection variable which was initialized when the constructor was called
     * @return connection
     */
    public Connection getConnection() {
        return this.connection;
    }
}
