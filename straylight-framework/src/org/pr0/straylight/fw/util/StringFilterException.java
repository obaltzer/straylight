/* $Id: StringFilterException.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * This exception is thrown when a StringFilter checks a string and a
 * completly illegal character combination is found in the string the
 * filter cannot fix. The exception stores special information about the
 * throwing filter which can be requested by the caller of the checker.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class StringFilterException extends Exception
{
    private int pos;
    private String name;
    private String msg;
    
    /**
     * initalize the exception with all neccessary information.
     *
     * @param pos the position in the string where the error occured
     * @param name the name of the filter
     */
    public StringFilterException(int pos, String name)
    {
        this.name = name;
        this.pos = pos;
        this.msg = "No message provided";
    }
    
    /**
     * initalize the exception with all neccessary information.
     *
     * @param pos the position in the string where the error occured
     * @param name the name of the filter
     * @param msg an additional message to the error
     */
    public StringFilterException(int pos, String name, String msg)
    {
        this.name = name;
        this.pos = pos;
        this.msg = msg;
    }

    /**
     * returns the name of the filter
     */
    public String getFilterName()
    {
        return name;
    }

    /**
     * returns the position in the string where the error occures.
     */
    public int getErrorPosition()
    {
        return pos;
    }
    
    /**
     * returns the message provided with the exception.
     */
    public String getMessage()
    {
        return msg;
    }
}
