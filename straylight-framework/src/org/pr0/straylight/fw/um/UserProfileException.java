/* $Id: UserProfileException.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

package org.pr0.straylight.fw.um;

/**
 * The UserProfileException will be thrown during the creation of a User if
 * one or more fields containing invalid values.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public class UserProfileException extends Exception
{
    /**
     * the reason why this exception was thrown
     */
    private String reason;
    
    /**
     * the name of the field which was invalid.
     */
    private String field;
    
    /**
     * instantiate the exception and generates a reason message with the
     * provided fieldname.
     *
     * @param field the field in the user profile which is has an invalid
     *              value
     */
    public UserProfileException(String field)
    {
        this.field = field;
        this.reason = "The value of field '" + field 
                    + "' is invalid in the user "
                    + "profile.";
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

    /**
     * returns the name of the field which was invalid and the reason for
     * the exception to be thrown.
     *
     * @return a string containing the name of the field
     */
    public String getField()
    {
        return this.field;
    }
} 
