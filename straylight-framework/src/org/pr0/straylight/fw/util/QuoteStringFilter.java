/* $Id: QuoteStringFilter.java,v 1.1 2001/08/26 02:11:18 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * <p>
 *  The QuoteStringFilter can be used to quote strings with '&gt; ' on the
 *  beginning of each line. 
 * </p>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2001/08/26 02:11:18 $
 */
public class QuoteStringFilter implements StringFilter
{
    public String filter(String input)
                  throws StringFilterException
    {
        String retval = input;
        // replace all \n with ' '
        
        retval = "> " + retval;
        int cpos = 0;
        boolean end = false;
        while(!end)
        {
            // insert the quote string
            if((cpos = retval.indexOf('\n', cpos)) != -1)
            {
                // replace the found char
                retval = retval.substring(0, cpos) + "\n> "
                       + retval.substring(cpos + 1, retval.length());
                cpos++;
            }
            else
                end = true;
            
        }
     
        return retval;
    }
}
