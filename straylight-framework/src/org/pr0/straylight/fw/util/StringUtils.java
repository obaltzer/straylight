/* $Id: StringUtils.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

import java.util.Vector;

/**
 * This class provides some static functions for processing strings.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class StringUtils
{
    /**
     * splits up a string at the given string and returns the separated
     * strings in an array. If the input string is null the result will be
     * null, too.
     *
     * @param input the string to be splitted up
     * @param split the string which splits the string
     * @return an array of strings
     */
    public static String[] split(String input, String split)
    {
        String retval[] = {};
     
        if(input != null)
        {
            // create temporary vector
            Vector substrings = new Vector();
            if(split != null)
            {
                int pos = 0;
                int fpos = 0;
                int slen = split.length();
                // as long we find the pattern in the string
                while((fpos = input.indexOf(split, pos)) >= 0)
                {
                    // put the strings from pos to fpos into the substrings
                    // vector
                    substrings.add(input.substring(pos, fpos));
                    pos = fpos + slen;
                }
                // put the rest of the string in the vector too
                substrings.add(input.substring(pos, input.length()));
            }
            else
            {
                // put the whole string in the vector
                substrings.add(new String(input));
            }
            // return value is the array of the vector
            retval = (String[]) substrings.toArray(retval);
        }
        // else return value is an empty array
        return retval;
    }
    
    /**
     * joins the strings of an array together with the specified string
     * between them. If the array has the length 0 the method will return
     * null for the string.
     *
     * @param input an array of strings
     * @param sep the separator between the strings
     * @return the combined string
     */
    public static String join(String input[], String sep)
    {
        String retval = null;

        if(input.length > 0)
        {
            retval = input[0];
            for(int i = 1; i < input.length; i++)
            {
                retval += sep + input[i];
            }
        }
        return retval;
    }

    /**
     * parses the Path and returns the subdirs as an Array of
     * Strings.<br><br>
     * Example:
     * <pre>
     *   String[] dirs=getPathAsList("/projects/straylight");
     * </pre>
     * This will return {"projects","straylight"}.
     *
     * @param path the Path string should be parsed.
     * @return an array of the several strings
     */
    public static String[] getPathAsList(String path) 
    {
        String[] retval = {};
        String dirs = path;
     
        // return an empty array on dirs==null
        if(dirs != null)
        {
            int startpos = 0;
            int endpos = 0;
            Vector subdirs = new Vector();
            // The following condition is a litlebit complex, but it works,
            // the last (endpos=dirs.length())>0) should only be perfomed
            // one time, because after the loop startpos=endpos and
            // startpos is no longer less than dirs.length()    
            while(startpos >= 0 && startpos < dirs.length()
                    &&((endpos = dirs.indexOf('/', endpos + 1)) > 0
                        || (endpos = dirs.length()) > 0))
            {
                // get the subfolder name
                String subd = dirs.substring(startpos + 1, endpos);

                // add the subfolder to the list
                subdirs.add(subd);

                // set startpos to endpos to get the next one
                startpos = endpos;
            }
            // convert the Vector-object to a list of Strings
            retval = (String[]) subdirs.toArray(retval);
        }
        return retval;
    } 
}
