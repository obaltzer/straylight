/* $Id: UserExistsException.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

package org.pr0.straylight.fw.um;

/**
 * The UserExistsException is thrown by the UserManager if a user should be
 * created but the given username is already in use by another user.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public class UserExistsException extends Exception
{
    /**
     * the message
     */
    private String reason;

    /**
     * instantiate the exception object and creates a default message with
     * the given username.
     *
     * @param username the username of the user which already exists
     */
    public UserExistsException(String username)
    {
        this.reason = "The username '" + username 
                    + "' is already in use by "
                    + "another user on this system.";
    }

    /**
     * returns the constructed message.
     *
     * @return the reason message
     */
    public String getMessage()
    {
        return this.reason;
    }
}
