/* $Id: ForumException.java,v 1.2 2001/08/26 02:09:12 racon Exp $ */

package org.pr0.straylight.fs;

/**
 * An exception thrown by the Forum object if there was a problem during 
 * the database access.
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:12 $
 */
class ForumException extends Exception
{
    private String msg;
 
    public ForumException(String msg)
    {
        this.msg = msg;
    }
 
    public String getMessage()
    {
        return this.msg;
    }
}
