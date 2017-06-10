package slamma.pst.events.Backlog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {		

	static String dbURL = "jdbc:mysql://localhost:3306/pst" 
    	    + "?verifyServerCertificate=false" //  bypassing the certificate validation
    	    + "&useSSL=false"
    	    + "&requireSSL=false";
	
	
    public static void main(String[] args) {
    	Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
    	try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
//            conn = DriverManager.getConnection("jdbc:mysql://localhost", "Slamma", "george");
            conn = DriverManager.getConnection(dbURL, "Slamma", "george");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM pst.type");
            while(rs.next()) {
            	System.out.println(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	if (rs != null) {
        		try {
        			rs.close();
        		} catch (SQLException sqlEx) {
        			sqlEx.printStackTrace();
        		}
        		rs = null;
        	}
        	if (stmt != null) {
        		try {
        			stmt.close();
        		} catch (SQLException sqlEx) {
        			sqlEx.printStackTrace();
        		}
        	}
        }
    }
}
