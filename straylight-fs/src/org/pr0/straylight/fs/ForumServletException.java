/* $Id: ForumServletException.java,v 1.2 2001/08/26 02:09:13 racon Exp $ */

package org.pr0.straylight.fs;

/**
 * An exception thrown by RequestHandlers of the ForumServlet if an error
 * occures during processing the request. The ForumServlet usually catches
 * this Exception and generates an error page. The exception needs an title
 * and a message content to be thrown. This structure is useful when
 * generating error pages, so the title of the error page is given by the
 * exception. The exception also supports the extra specification of
 * details, e.g. other exception messages, etc. which should be not shown
 * to the user.
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:13 $
 */
class ForumServletException extends Exception
{
    private String msg;
    private String title;
    private String details;
 
    public ForumServletException(String title, String msg)
    {
        this.title = title;
        this.msg = msg;
        this.details = "no details";
    }
 
    public ForumServletException(String title, String msg, String details)
    {
        this.title = title;
        this.msg = msg;
        this.details = details;
    }
 
    public String getMessage()
    {
        return this.msg;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDetails()
    {
        return this.details;
    }
}
