/* $Id: ArrayUtils.java,v 1.3 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

import java.util.Vector;

/**
 * This class provides some static functions for processing arrays.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:58 $
 */
public class ArrayUtils
{
    /**
     * removes the specified value from given array end returns the resizes
     * array.
     *
     * @param input the input array
     * @param value the value which should be deleted from the
     *        array
     * @return a resized array
     */
    public static int[] remove(int[] input, int value)
    {
        int[] tmp = input;

        int counter = 0;
        
        for(int i = 0; i < tmp.length; i++)
            if(tmp[i] == value) 
                counter++;
        
        int[] retval = new int[tmp.length - counter];
    
        counter = 0;
        for(int i = 0; i < tmp.length; i++)
        {
            if(tmp[i] != value)
            {
                retval[counter] = tmp[i];
                counter++;
            }
        }
        return retval; 
    }

    /**
     * adds the spcific value to the given array and returns the resized
     * array.
     *
     * @param input the input array
     * @param value the value which should be inserted
     * @param a resized array
     */
    public static int[] add(int[] input, int value)
    {
        int[] retval = new int[input.length + 1];

        System.arraycopy(input, 0, retval, 0, input.length);
        retval[input.length] = value;
  
        return retval;
    }
    
    /**
     * returns the position of the specified value in the given array.
     * 
     * @param input the input array in which the value should be 
     *              searched
     * @return first position of the value in array or -1 if the value was
     *         not found
     */
    public static int indexOf(int[] input, int value)
    {
        for(int i = 0; i < input.length; i++)
            if(input[i] == value)
                return i;
        return -1;
    }
}
