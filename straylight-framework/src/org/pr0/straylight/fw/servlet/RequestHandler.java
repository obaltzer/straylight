/* $Id: RequestHandler.java,v 1.2 2001/09/27 16:39:54 racon Exp $ */

package org.pr0.straylight.fw.servlet;

import java.util.Hashtable;

/**
 * An interface that defines the methods of a class which implements and
 * acts as a RequestHandler. The RequestHandler is an object which handles
 * the request and generates the context object for the response page.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:54 $
 */
public interface RequestHandler
{
    /**
     * returns the context for the response page.
     */
    public Hashtable getContext();

    /**
     * returns the name for the response page template.
     */
    public String getTemplateName();
}
