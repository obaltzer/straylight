/* $Id: ErrorPageRequest.java,v 1.2 2001/08/26 02:09:13 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.servlet.RequestHandler;

/**
 * This class handles the ErrorPageRequest and generates a context and
 * provides a template name.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:13 $
 */
class ErrorPageRequest implements RequestHandler
{
    Hashtable context;
    String templatename;

    /**
     * generates the context from the given parameters.
     *
     * @param errortitle the tile of the error
     * @param errormessage the message of the error
     */
    protected ErrorPageRequest(String errortitle, String errormsg)
    {
        Category.getInstance(this.getClass()).error("Error: "
                                                    + errortitle);
        context = new Hashtable();
        context.put("ErrorTitle", errortitle);
        context.put("ErrorMessage", errormsg);
        templatename = "Error";
    }

    public Hashtable getContext()
    {
        return context;
    }

    public String getTemplateName()
    {
        return templatename;
    }
}
