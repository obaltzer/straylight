/* $Id: DBConnectionPoolException.java,v 1.2 2001/09/27 16:39:53 racon Exp $ */

package org.pr0.straylight.fw.db;

/**
 * The DBConnectionPoolException will be thrown when there was an error occured
 * during the processing of methods managing the DBConnectionPool.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:53 $
 */
public class DBConnectionPoolException extends Exception
{
    private String reason = "";
    
    public DBConnectionPoolException(String reason)
    {
        this.reason = reason;
    }
 
    public String getMessage()
    {
        return reason;
    }
} 
