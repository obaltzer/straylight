/* $Id: HTMLStringFilter.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;

/**
 * <p>
 *  This StringFilter will check the string for any kind of HTML tag. If
 *  there is an HTML tag found the filter will throw an exception. This
 *  filter should be used if you want to force no HTML in a string.
 * </p>
 * <p>
 *  The rules are: 
 * </p>
 * <ul>
 *  <li>any '&lt;' sign followed by a letter or a '/' is a potential tag</li>
 *  <li>each '&amp;' sign followed by a letter (XXX or sharp) is a
 *      potential entity
 *  </li> 
 * </ul>
 * <p>
 *  If one of these signs is in the string the filter will throw an
 *  exception.
 * </p>
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class HTMLStringFilter implements StringFilter
{
    // Pre-compiled regular expression '<(\w|/)'

    private static char[] tagPatternInstructions =
    {
        0x007c, 0x0000, 0x001a, 0x0041, 0x0001, 0x0004, 0x003c, 0x0028,
        0x0001, 0x0003, 0x007c, 0x0000, 0x0006, 0x005c, 0x0077, 0x000a,
        0x007c, 0x0000, 0x0007, 0x0041, 0x0001, 0x0004, 0x002f, 0x0029,
        0x0001, 0x0003, 0x0045, 0x0000, 0x0000, 
    };
    
    // Pre-compiled regular expression '&\w'
    
    private static char[] entityPatternInstructions =
    {
        0x007c, 0x0000, 0x000a, 0x0041, 0x0001, 0x0004, 0x0026, 0x005c,
        0x0077, 0x0003, 0x0045, 0x0000, 0x0000,
    };
    
    public String filter(String input) throws StringFilterException
    {
        String retval = input;
     
        if(retval != null)
        {
            RE tag = new RE(new REProgram(tagPatternInstructions));
            RE entity = new RE(new REProgram(entityPatternInstructions));
            if(tag.match(input))
            {
                throw new StringFilterException(
                    tag.getParenStart(0),
                    "HTMLStringFilter",
                    "Tag element in string."
                );
            }
            if(entity.match(input))
            {
                throw new StringFilterException(
                    entity.getParenStart(0),
                    "HTMLStringFilter",
                    "Entity element in string."
                );
            }
        }
        return retval;
    }
}
     
