/* $Id: UserNotFoundException.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

/* CHANGES
 *
 * 2001-06-26   ob      initial creation of the exception class
 */

package org.pr0.straylight.fw.um;

/**
 * The UserNotFoundException will be thrown if the UserManager cannot find
 * a requested user, either in getting, updating or deleting a user. It
 * will automatically generate a message if the username or the Id of the
 * user is passed to this Exception.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public class UserNotFoundException extends Exception
{
    /**
     * the reason why this exception was thrown
     */
    private String reason;

    /**
     * instanciate the exception and generates a reason message with the
     * provided user Id.
     *
     * @param id the Id of the user the UserManager was searching
     */
    public UserNotFoundException(int id)
    {
        this.reason = "The user with ID " + Integer.toString(id) 
                    + " cannot be found in data source.";
    }
    
    /**
     * instanciate the exception and generates a reason message with the
     * provided username.
     *
     * @param username the username of the user the UserManager was
     *                 searching
     */
    public UserNotFoundException(String username)
    {
        this.reason = "The user with username '" + username 
                    + "' cannot be found in data source.";
    }

    /**
     * instanciate the exception and generates a reason message with the
     * provided user Id and the provided consequence.
     *
     * @param id the Id of the user the UserManager was searching
     * @param consequence what that means that the user cannot be found
     */
    public UserNotFoundException(int id, String consequence)
    {
        this.reason = "The user with ID " + Integer.toString(id) 
                    + " cannot be found in data source. " 
                    + consequence;
    }
    
    /** 
     * instanciate the exception and generates a reason message with the
     * provided username and the provided consequence.
     *
     * @param username the username of the user the UserManager was
     *                 searching
     * @param consequence what that means that the user cannot be found
     */
    public UserNotFoundException(String username, String consequence)
    {
        this.reason = "The user with username '" + username 
                    + "' cannot be found in data source. "
                    + consequence;
    }

    /**
     * returns the automatically generated message.
     *
     * @return the message
     */
    public String getMessage()
    {
        return this.reason;
    }
} 
