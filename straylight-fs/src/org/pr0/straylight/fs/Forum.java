/* $Id: Forum.java,v 1.3 2001/09/27 16:39:50 racon Exp $ */

package org.pr0.straylight.fs;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.pr0.straylight.fw.db.DBConnection;

/**
 * The Forum class represents a forum in the context of the ForumServlet.
 * It holds some information about the forum and will be cached by the
 * ForumServlet.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:50 $
 */
class Forum
{
    /**
     * the Id of the forum.
     */
    private long id;
    
    /**
     * the long title of the forum.
     */
    private String title;
    
    /**
     * a detailed description of the forum.
     */
    private String description;
    
    /**
     * the shortname of the forum which is used in the url
     */
    private String shortname;
    
    /**
     * an optional homepage URL
     */
    private String homepage;
    
    /**
     * indicates if the forum is read restricted.
     */
    private boolean readrestricted;
    
    /**
     * indicates if it is allowed to register for the forum.
     */
    private boolean readregisterallowed;
    
    /**
     * indicates if it needs an confirmation by the forum admin for new
     * users
     */
    private boolean readregisterconfirm;
    
    /**
     * indicates if the forum is post restricted.
     */
    private boolean postrestricted;
    
    /**
     * indicates if it is allowed to register for this forum.
     */
    private boolean postregisterallowed;
    
    /**
     * indicates if the registration of an user needs the comfirmation by
     * an forum admin.
     */
    private boolean postregisterconfirm;
    
    /**
     * Initalizes the Forum object with the necessary data from the
     * database.
     *
     * @param dbcon the open database connection to get the data
     * @param shortname the shortname of the forum which should be
     *                  initalized
     * @throws ForumException on error during database access
     */
    protected Forum(DBConnection dbcon, String shortname)
                    throws ForumException
    {
        try
        {
            ResultSet rs = dbcon.executeQuery(
                "SELECT DISTINCT Id, Title, "
                + "Description, Shortname, Homepage, "
                + "ReadRestricted, "
                + "ReadRegisterAllowed, "
                + "ReadRegisterConfirm, "
                + "PostRestricted, "
                + "PostRegisterAllowed, "
                + "PostRegisterConfirm "
                + "FROM Forum WHERE "
                + "Shortname='" + shortname + "'"
            );
            
            if(rs.next())
            {
                this.id = rs.getLong(1);
                this.title = rs.getString(2);
                this.description = rs.getString(3);
                this.shortname = rs.getString(4);
                this.homepage = rs.getString(5);
                this.readrestricted = rs.getInt(6) == 1 ? true : false;
                this.readregisterallowed = 
                    rs.getInt(7) == 1 ? true : false;
                this.readregisterconfirm =
                    rs.getInt(8) == 1 ? true : false;
                this.postrestricted = rs.getInt(9) == 1 ? true : false;
                this.postregisterallowed = 
                    rs.getInt(10) == 1 ? true : false;
                this.postregisterconfirm = 
                    rs.getInt(11) == 1 ? true : false;
            }
            else
            {
                throw new ForumException("No forum with shortname '"
                                         + shortname
                                         + "' found in database.");
            }
        }
        catch(SQLException e)
        {
            throw new ForumException("Cannot get information about "
                                     + "forum with shortname '"
                                     + shortname + "' from database: "
                                     + e.getMessage());
        }
    }                         
                              
    /**
     * Initalize the Forum object with the necessary data from the
     * database.
     *
     * @param dbcon the temporary connection to the database
     * @param id the Id of the requested forum
     * @throws ForumException on error during database access
     */
    protected Forum(DBConnection dbcon, long id)
                    throws ForumException
    {
        try
        {
            ResultSet rs = dbcon.executeQuery(
                "SELECT DISTINCT Id, Title, "
                + "Description, Shortname, Homepage, "
                + "ReadRestricted, "
                + "ReadReagisterAllowed, "
                + "ReadRegisterConfirm, "
                + "PostRestricted, "
                + "PostRegisterAllowed, "
                + "PostRegisterConfirm "
                + "FROM Forum WHERE "
                + "Id=" + id
            );
      
            if(rs.next())
            {
                this.id = rs.getLong(1);
                this.title = rs.getString(2);
                this.description = rs.getString(3);
                this.shortname = rs.getString(4);
                this.homepage = rs.getString(5);
                this.readrestricted = rs.getInt(6) == 1 ? true : false;
                this.readregisterallowed = 
                    rs.getInt(7) == 1 ? true : false;
                this.readregisterconfirm = 
                    rs.getInt(8) == 1 ? true : false;
                this.postrestricted = rs.getInt(9) == 1 ? true : false;
                this.postregisterallowed = 
                    rs.getInt(10) == 1 ? true : false;
                this.postregisterconfirm = 
                    rs.getInt(11) == 1 ? true : false;
            }
            else
            {
                throw new ForumException("No forum with ID '" + id
                                         + "' found in database.");
            }
        }
        catch(SQLException e)
        {
            throw new ForumException("Cannot get information about "
                                     + "forum with ID '" + id 
                                     + "' from database.");
        }
    }

    /**
     * returns the Id of the forum.
     */
    protected long getId()
    {
        return id;
    }

    /**
     * returns the title of this forum.
     */
    protected String getTitle()
    {
        return title;
    }
    
    /**
     * returns the description of this forum.
     */
    protected String getDescription()
    {
        return description;
    }

    /**
     * returns the shortname of the forum.
     */
    protected String getShortname()
    {
        return this.shortname;
    }

    /**
     * returns the homepage URL of the forum.
     */
    protected String getHomepage()
    {
        return this.homepage;
    }

    /**
     * returns true if this forum is read restricted.
     */
    protected boolean isReadRestricted()
    {
        return this.readrestricted;
    }

    /**
     * returns true if it is allowed to register for read access to this
     * forum.
     */
    protected boolean isReadRegistrationAllowed()
    {
        return this.readregisterallowed;
    }

    /**
     * returns true if the read registration for this forum need a
     * confirmation.
     */
    protected boolean confirmReadRegistration()
    {
        return this.readregisterconfirm;
    }
    
    /**
     * returns true if this forum is post restricted.
     */
    protected boolean isPostRestricted()
    {
        return this.postrestricted;
    }

    /**
     * returns true if it is allowed to register for post access to this
     * forum.
     */
    protected boolean isPostRegistrationAllowed()
    {
        return this.postregisterallowed;
    }

    /**
     * returns true if the post registration for this forum need a
     * confirmation.
     */
    protected boolean confirmPostRegistration()
    {
        return this.postregisterconfirm;
    }
}
