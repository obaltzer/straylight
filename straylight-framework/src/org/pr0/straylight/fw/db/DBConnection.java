/* $Id: DBConnection.java,v 1.2 2001/09/27 16:39:53 racon Exp $ */

package org.pr0.straylight.fw.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class provides some extensions to the common Connection.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:53 $
 */
 
public class DBConnection
{
    /**
     * the default Java database connection.
     */
    private Connection dbcon;
 
    /**
     * Constructor opens a new database connection with the given URL.
     *
     * @param connectionurl the URL used to connect the database
     * @throws java.sql.SQLException when the connection can't be created
     */ 
    public DBConnection(String connectionurl) throws SQLException
    {
        dbcon = DriverManager.getConnection(connectionurl);
    }
 
    /**
     * Constructor opens a new database connection with the given URL and
     * additional username and password.
     *
     * @param connectionurl the URL used to connect the database
     * @param username username of the database user
     * @param password password of the database user
     * @throws java.sql.SQLException when the connection can't be created
     */ 
    public DBConnection(String connectionurl, String username, 
                        String password)
                        throws SQLException
    {
        dbcon = 
            DriverManager.getConnection(connectionurl,username,password);
    }
 
    /**
     * executes the specified SQL statement.
     *
     * @param sql the SQL statement as a string
     * @throws java.sql.SQLException when there was an error during execution
     * @return a ResultSet with the results of the query
     */
    public ResultSet executeQuery(String sql) throws SQLException
    {
        Statement stmt = dbcon.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
  
        return rs;
    }
 
    /**
     * returns the database connection of this object
     *
     * @return the Connection object represents the open connection
     */
    public Connection getConnection()
    {
        return dbcon;
    }
 
    /**
     * this function will be called by the garbage collector and will close the
     * database connection if this is open.
     *
     * @throws java.sql.SQLException
     */
    protected void finalize() throws SQLException
    {
        if(!dbcon.isClosed()) dbcon.close();
    } 
}
