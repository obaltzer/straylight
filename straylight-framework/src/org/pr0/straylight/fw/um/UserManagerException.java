/* $Id: UserManagerException.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

/* CHANGES
 *
 * 2001-06-26   ob      initial creation of the exception class
 */

package org.pr0.straylight.fw.um;

/**
 * The UserManagerException is usually thrown if there was an error during
 * the performing of source accesses or a misconfiguration. For all logical
 * errors according to the management of users are other Exceptions
 * defined. This class also defines some functions to get information about
 * the original reason, why the exception was thrown.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public class UserManagerException extends Exception
{
    /**
     * the original reason of interruption if available
     */
    private String originalReason = null;

    /**
     * the reason why this exceptions was thrown
     */
    private String reason = null;

    /**
     * instanciate the exception just with a reason message.
     *
     * @param reason the reason, why the exception was thrown
     */
    public UserManagerException(String reason)
    {
        this.reason = reason;
    }

    /**
     * instanciate the exception with a reason message and with an original
     * message.
     * 
     * @param reason the reason why this exception was thrown
     * @param original the original reason, for example of an other
     *                 exception
     */
    public UserManagerException(String reason, String original)
    {
        this.reason = reason;
        this.originalReason = original;
    }

    /**
     * returns the reason message.
     *
     * @return the reason why this exception was thrown
     */
    public String getMessage()
    {
        return this.reason;
    }

    /**
     * returns the original message if defined otherwise null.
     *
     * @return the original reason message or null
     */
    public String getOriginal()
    {
        return originalReason;
    }
}
    

