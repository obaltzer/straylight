/* $Id: StringFilterChain.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

import java.util.Vector;

/**
 * Implements the StringFilterChain which can be used to process a string
 * with several filters. These filters can modify the string or they throw
 * an exception on an illegal string content.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class StringFilterChain
{
    private Vector filterchain;

    /**
     * initalizes the chain.
     */
    public StringFilterChain()
    {
        filterchain = new Vector();
    }

    /**
     * initalizes the chain with one a filter.
     *
     * @param filter the first filter in the chain.
     */
    public StringFilterChain(StringFilter filter)
    {
        filterchain = new Vector();
        this.add(filter);
    }

    /**
     * initalizes the chain with an other chain.
     *
     * @param chain the chain which should be used for initalization
     */
    public StringFilterChain(StringFilterChain chain)
    {
        filterchain = new Vector(chain.filterchain);
    }

    /**
     * adds a StringFilter to the chain and returns the position of the
     * filter in the chain.
     *
     * @param filter the filter to be added
     * @return the position where the filter was added
     */
    public int add(StringFilter filter)
    {
        int retval = filterchain.size();
        if(filter != null) 
        {
            filterchain.add(filter);
        }
        return retval;
    }

    /**
     * removes the filter at the given position from the chain. If the
     * position is either negativ or greater than the numer of filters in
     * the chain the chain keeps unchanged.
     *
     * @param pos the position of the filter to be removed
     */
    public void remove(int pos)
    {
        if(pos > 0 && pos < filterchain.size())
        {
            filterchain.remove(pos);
        }
    }

    /**
     * removes the specified filter from the chain. If the filter could not
     * be found in the chain the chain keeps unchanged.
     *
     * @param filter the filter to be removed
     */
    public void remove(StringFilter filter)
    {
        if(filter != null)
        {
            filterchain.remove(filter);
        }
    }
    
    /**
     * returns the position of the specified filter. If the specified
     * filter is not in the chain it returns -1.
     *
     * @param filter the filter to be found
     * @return the position of the filter in the chain
     */
    public int getFilterPosition(StringFilter filter)
    {
        int retval = -1;
        if(filter != null)
        {
            retval = filterchain.indexOf(filter);
        }
        return retval;
    }

    /**
     * checks the given string with all filters in the chain and returns
     * the maybe modified string. It will throw an exception if an fatal
     * error in the string occures.
     *
     * @param input the string to check
     * @return the maybe modified string
     * @throws StringFilterException when an illegal character combination
     *                               was found.
     */
    public String filter(String input) throws StringFilterException
    {
        String retval = input;
        if(retval != null)
        {
            int filtercount = filterchain.size();
            for(int i = 0; i < filtercount; i++)
            {
                StringFilter filter = (StringFilter)filterchain.get(i);
                retval = filter.filter(retval);
            }
        }
        return retval;
    }
}   
