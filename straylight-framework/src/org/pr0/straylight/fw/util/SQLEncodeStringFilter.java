/* $Id: SQLEncodeStringFilter.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * <p>
 *  This StringFilter will modify the given string and will escape all
 *  characters used in SQL. 
 * </p>
 * <p>
 *  These characters are: 
 * </p>
 * <ul>
 *  <li>\ -&gt; \\</li>
 *  <li>' -&gt; \'</li>
 *  <li>" -&gt; \"</li>
 *  <li>% -&gt; \%</li>
 *  <li>_ -&gt; \_</li>
 * </ul>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class SQLEncodeStringFilter implements StringFilter
{
    public String filter(String input) throws StringFilterException
    {
        String retval = input;
     
        if(retval != null)
        {
            // at first replace all backslashes to prevent double escaping
            String toEscape[] = {"\\", "'", "\"", "%", "_"};
            for(int i = 0; i < toEscape.length; i++)
            { 
                int pos = 0;
                int findpos = 0;
                while((findpos = retval.indexOf(toEscape[i], pos)) > -1)
                {
                    String newstring = retval.substring(0, findpos);
                    newstring += "\\" + toEscape[i];
                    newstring += 
                        retval.substring(findpos + 1, retval.length());
                    retval = newstring;
                    pos = findpos + 2;
                }
            }
        }
        return retval;
    }
}
     
