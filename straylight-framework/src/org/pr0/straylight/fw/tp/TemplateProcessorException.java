/* $Id: TemplateProcessorException.java,v 1.2 2001/09/27 16:39:55 racon Exp $ */

package org.pr0.straylight.fw.tp;

/**
 * This exception will be thrown by the TemplateProcessor class when an error
 * occured.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:55 $
 */
 
public class TemplateProcessorException extends Exception
{
    private String msg = "unkown reason";
 
    /**
     * The constructor of this exception expects a reason as a string, why
     * the exception is been thrown. This reason will be returned if the
     * getMessage method will be called.
     *
     * @param reason the reason why the exception is been thrown
     */  
    public TemplateProcessorException(String reason)
    {
        this.msg = reason;
        printStackTrace(System.err);
    }
 
    /**
     * Returns the message.
     */
    public String getMessage()
    {
        return this.msg;
    }
}
