/* $Id: HomepageStringFilter.java,v 1.1 2002/02/05 21:51:40 racon Exp $ */

package org.pr0.straylight.fw.util;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;

/**
 * <p>
 *  Checks for a valid homepage URL. 
 * </p>
 * <p>
 *  If the homepage URL is of an invalid format the filter will throw an
 *  StringFilterException.
 * </p>
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2002/02/05 21:51:40 $
 */
public class HomepageStringFilter implements StringFilter
{
    // Pre-compiled regular expression 
    // '^\s*(http|https):\/\/'
    // '([A-Za-z0-9]([A-Za-z0-9\-]*[A-Za-z0-9])?\.)*'
    // '([A-Za-z0-9]([A-Za-z0-9\-]*[A-Za-z0-9])?)'
    // '(\/.*)?\s*$'
    private static char[] homepagePatternInstructions = 
    {
        0x007c, 0x0000, 0x0117, 0x005e, 0x0000, 0x0003, 0x007c, 
        0x0000, 0x000c, 0x005c, 0x0073, 0x0003, 0x007c, 0x0000, 
        0x0003, 0x0047, 0x0000, 0xfff7, 0x007c, 0x0000, 0x0003, 
        0x004e, 0x0000, 0x0003, 0x0028, 0x0001, 0x0003, 0x007c, 
        0x0000, 0x000a, 0x0041, 0x0004, 0x0012, 0x0068, 0x0074, 
        0x0074, 0x0070, 0x007c, 0x0000, 0x000b, 0x0041, 0x0005, 
        0x0008, 0x0068, 0x0074, 0x0074, 0x0070, 0x0073, 0x0029, 
        0x0001, 0x0003, 0x0041, 0x0003, 0x0006, 0x003a, 0x002f, 
        0x002f, 0x007c, 0x0000, 0x0054, 0x0028, 0x0002, 0x0003, 
        0x007c, 0x0000, 0x0045, 0x005b, 0x0003, 0x0009, 0x0041, 
        0x005a, 0x0061, 0x007a, 0x0030, 0x0039, 0x007c, 0x0000, 
        0x002f, 0x0028, 0x0003, 0x0003, 0x007c, 0x0000, 0x0026, 
        0x007c, 0x0000, 0x0014, 0x005b, 0x0004, 0x000b, 0x0041, 
        0x005a, 0x0061, 0x007a, 0x0030, 0x0039, 0x002d, 0x002d, 
        0x007c, 0x0000, 0x0003, 0x0047, 0x0000, 0xffef, 0x007c, 
        0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 0x005b, 0x0003, 
        0x0009, 0x0041, 0x005a, 0x0061, 0x007a, 0x0030, 0x0039, 
        0x0029, 0x0003, 0x0006, 0x007c, 0x0000, 0x0003, 0x004e, 
        0x0000, 0x0003, 0x0041, 0x0001, 0x0004, 0x002e, 0x0029, 
        0x0002, 0x0003, 0x007c, 0x0000, 0x0003, 0x0047, 0x0000, 
        0xffaf, 0x007c, 0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 
        0x0028, 0x0004, 0x0003, 0x007c, 0x0000, 0x0041, 0x005b, 
        0x0003, 0x0009, 0x0041, 0x005a, 0x0061, 0x007a, 0x0030, 
        0x0039, 0x007c, 0x0000, 0x002f, 0x0028, 0x0005, 0x0003, 
        0x007c, 0x0000, 0x0026, 0x007c, 0x0000, 0x0014, 0x005b, 
        0x0004, 0x000b, 0x0041, 0x005a, 0x0061, 0x007a, 0x0030, 
        0x0039, 0x002d, 0x002d, 0x007c, 0x0000, 0x0003, 0x0047, 
        0x0000, 0xffef, 0x007c, 0x0000, 0x0003, 0x004e, 0x0000, 
        0x0003, 0x005b, 0x0003, 0x0009, 0x0041, 0x005a, 0x0061, 
        0x007a, 0x0030, 0x0039, 0x0029, 0x0005, 0x0006, 0x007c, 
        0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 0x0029, 0x0004, 
        0x0003, 0x007c, 0x0000, 0x0022, 0x0028, 0x0006, 0x0003, 
        0x007c, 0x0000, 0x0019, 0x0041, 0x0001, 0x0004, 0x002f, 
        0x007c, 0x0000, 0x000c, 0x002e, 0x0000, 0x0003, 0x007c, 
        0x0000, 0x0003, 0x0047, 0x0000, 0xfff7, 0x007c, 0x0000, 
        0x0003, 0x004e, 0x0000, 0x0003, 0x0029, 0x0006, 0x0006, 
        0x007c, 0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 0x007c, 
        0x0000, 0x000c, 0x005c, 0x0073, 0x0003, 0x007c, 0x0000, 
        0x0003, 0x0047, 0x0000, 0xfff7, 0x007c, 0x0000, 0x0003, 
        0x004e, 0x0000, 0x0003, 0x0024, 0x0000, 0x0003, 0x0045, 
        0x0000, 0x0000, 
    };

    public String filter(String input) throws StringFilterException
    {
        if(input != null)
        {
            RE homepagePattern = 
                new RE(new REProgram(homepagePatternInstructions));
                
            if(!homepagePattern.match(input))
            {
                throw new StringFilterException(0, "HomepageStringFilter",
                                                "Invalid homepage URL "
                                                + "format");
            }
            else
                return input;
        }
        else
            throw new StringFilterException(0, "HomepageStringFilter",
                                            "No homepage URL defined.");
    }
}

