/* $Id: ConfigurationException.java,v 1.2 2001/09/27 16:39:54 racon Exp $ */

package org.pr0.straylight.fw.servlet;

/**
 * This is a simple Exception class, which is thrown if an error occures because
 * of a wrong configuration.
 *
 * @author <a href="meilto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:54 $
 */ 
public class ConfigurationException extends Exception
{
    private String msg = "unknown reason";
 
    public ConfigurationException(String reason)
    {
        this.msg = reason;
    }

    public String getMessage()
    {
        return msg;
    }
}
 
