/* $Id: TextBlockFormatter.java,v 1.1 2002/02/05 21:51:40 racon Exp $ */

package org.pr0.straylight.fw.util;

import org.pr0.straylight.fw.util.StringUtils;

/**
 * <p>
 *  This class implements the <code>StringFilter</code> interface and
 *  should be used as a <code>StringFilter</code> to format text blocks to
 *  text blocks with a fixed width. It is possible to specify the width of
 *  a text block. An optional string could be also specified, so that lines
 *  starting with that string will not be broken.
 * </p>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2002/02/05 21:51:40 $
 */
public class TextBlockFormatter implements StringFilter
{
    /** 
     * the width to which the block should be adjusted. 
     * Default value is 80. 
     * */ 
    private int width = 80;
    
    /**
     * the <code>String</code> which indicated the lines that should not be
     * broken. Default value <code>null</code> for breaking all lines.
     */
    private String savedLinesPattern = null;

    /**
     * the default constructor does not do anything and uses the default
     * values.
     */
    public TextBlockFormatter() {}

    /**
     * initalize the TextBlockFormatter with a customized width for the
     * text block.
     *
     * @param width an <code>int</code> value greater or equal than
     *              <code>1</code>
     * @throws IllegalAgrumentException if the width values is out of range
     */
    public TextBlockFormatter(int width)
    {
        if(width >= 1)
            this.width = width;
        else
            throw new IllegalArgumentException("Width out of range, must "
                                               + "be equal or greater "
                                               + "than 1.");
    }
    
    /**
     * initalizes the <code>TextBlockFormatter</code> with a
     * <code>String</code> that will be used to
     * determine which lines should not be broken. The lines must start
     * with that string.
     *
     * @param pattern a <code>String</code> object or <code>null</code>
     */
    public TextBlockFormatter(String pattern)
    {
        this.savedLinesPattern = pattern;
    }
    
    /**
     * initializes the <code>TextBlockFormatter</code> with both a width
     * and a <code>String</code>.
     *
     * @param width an <code>int</code> value greater or equal than
     *              <code>1</code>
     * @param pattern a <code>String</code> object or <code>null</code>
     * @throws IllegalAgrumentException if the width values is out of range
     */
    public TextBlockFormatter(int width, String pattern)
    {
        if(width >= 1)
            this.width = width;
        else
            throw new IllegalArgumentException("Width out of range, must "
                                               + "be equal or greater "
                                               + "than 1.");
        this.savedLinesPattern = pattern;
    }
    
    /**
     * the implementation of the interface function <code>filter(String
     * input)</code> which is processing the text block and formats it.
     *
     * @param input the <code>String</code> of the text block which should
     *              be formatted
     * @throws StringFilterException if there was an error during
     *                               formatting
     * @return the <code>String</code> of the formatted text block
     */
    public String filter(String input) throws StringFilterException
    {
        // the word separators
        String[] wordsep = {" ", "-"};
        // the result String
        StringBuffer res = new StringBuffer();
        // split the String at new lines
        String[] lines = StringUtils.split(input, "\n");
        
        for(int i = 0; i < lines.length; i++)
        {
            if((savedLinesPattern != null &&
                lines[i].startsWith(savedLinesPattern)) 
                || lines[i].length() <= width)
            {
                res.append(lines[i] + "\n");
            }
            else if(lines[i].length() > width)
            {
                boolean found = false;
                int pos = 0;
                while(!found && pos != width)
                {
                    pos++;
                    for(int j = 0; j < wordsep.length; j++)
                    {
                        if(lines[i].startsWith(wordsep[j], width - pos))
                            found = true;
                    }
                }
                pos--;
                if(found)
                {
                    res.append(lines[i].substring(0, width - pos) + "\n");
                    // add one aditional line
                    String[] oldlines = lines;
                    lines = new String[oldlines.length + 1];
                    // copy first part of array
                    System.arraycopy(oldlines, 0, lines, 0, i + 1);
                    // copy rest of the lines behind the inserted line
                    System.arraycopy(oldlines, i + 1, lines, i + 2,
                                     oldlines.length - (i + 1));
                    // insert the line
                    lines[i + 1] = lines[i].substring(width - pos, 
                                                      lines[i].length());
                }
                else
                    res.append(lines[i] + "\n");
            }
        }
        // delete last \n
        res.deleteCharAt(res.length() - 1);
        return res.toString();
    }
}
