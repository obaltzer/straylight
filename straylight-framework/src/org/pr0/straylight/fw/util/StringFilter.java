/* $Id: StringFilter.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * This interface has to be implemented by String Filter which want
 * to be included into a StringFilterChain.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */

public interface StringFilter
{
    /**
     * filters the string 'input' and creates an output string or throws an
     * exception. How the functions works depends on the implementation.
     * The StringFilterException should be thrown when an illegal character
     * combination in the string was found.
     *
     * @param input the string to check
     * @return a string passing this filter
     * @throws StringFilterException when an illegal character combination
     *                               was found
     */
    public String filter(String input) throws StringFilterException; 
}  
