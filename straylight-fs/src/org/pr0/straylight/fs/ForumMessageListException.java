/* $Id: ForumMessageListException.java,v 1.2 2001/08/26 02:09:12 racon Exp $ */

package org.pr0.straylight.fs;

/**
 * This exception is thrown when an error occured during the creation of an
 * ForumMessageList. It does not support a special message and is just used
 * to indicate a general error.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:12 $
 */
class ForumMessageListException extends Exception
{
    /**
     * initalizes the exception.
     */
    protected ForumMessageListException()
    {
        // just do nothing
    }

    /**
     * returns a general message
     *
     * @return the message
     */
    public String getMessage()
    {
        return "An error occures while generating a message list.";
    }
}
