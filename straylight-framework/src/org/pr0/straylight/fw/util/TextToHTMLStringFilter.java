/* $Id: TextToHTMLStringFilter.java,v 1.2 2001/08/26 02:11:18 racon Exp $ */

package org.pr0.straylight.fw.util;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;

/**
 * <p>
 *  This StringFilter extends text strings with HTML tags for linking HTTP,
 *  FTP resources or eMail addresses and replaces international characters
 *  or HTML reserved signs with entities. 
 * </p>
 * <p>
 *  Recognized patterns are: 
 * </p>
 * <ul>
 *  <li>http:// - for http resources</li>
 *  <li>https:// - for secure http resources</li>
 *  <li>ftp:// - for ftp resources</li>
 *  <li>mailto: - for eMail addresses</li>
 *  <li>Servernames starting with www, web, w3, or ftp</li>
 *  <li>&ouml; -&gt; &amp;ouml;</li>
 *  <li>&Ouml; -&gt; &amp;Ouml;</li>
 *  <li>&auml; -&gt; &amp;auml;</li>
 *  <li>&Auml; -&gt; &amp;Auml;</li>
 *  <li>&uuml; -&gt; &amp;uuml;</li>
 *  <li>&Uuml; -&gt; &amp;Uuml;</li>
 *  <li>&szlig; -&gt; &amp;iszlig;</li>
 *  <li>&lt; -&gt; &amp;lt;</li>
 *  <li>&gt; -&gt; &amp;gt;</li>
 *  <li>(c) -&gt; &amp;copy;</li>
 *  <li>(C) -&gt; &amp;copy;</li>
 *  <li>&amp; -&gt; &amp;amp;</li>
 * </ul>
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:11:18 $
 */
public class TextToHTMLStringFilter implements StringFilter
{
    // XXX make a correct rule here !!!
    // 
    // Pre-compiled regular expression '((http|ftp|https)://[^ \)]+)|
    // ((www|web|w3|ftp)\.[^ \)]+)|((mailto):[^ @]+@[a-zA-Z0-9\-\.]+)'
    // 
    // it is used to recognize potential links in the string
    private static char[] URLDetectorPatternInstructions = 
    {
        0x007c, 0x0000, 0x004b, 0x0028, 0x0001, 0x0003, 0x007c, 0x0000,
        0x0042, 0x0028, 0x0002, 0x0003, 0x007c, 0x0000, 0x000a, 0x0041,
        0x0004, 0x001b, 0x0068, 0x0074, 0x0074, 0x0070, 0x007c, 0x0000,
        0x0009, 0x0041, 0x0003, 0x0011, 0x0066, 0x0074, 0x0070, 0x007c,
        0x0000, 0x000b, 0x0041, 0x0005, 0x0008, 0x0068, 0x0074, 0x0074,
        0x0070, 0x0073, 0x0029, 0x0002, 0x0003, 0x0041, 0x0003, 0x0006,
        0x003a, 0x002f, 0x002f, 0x005b, 0x0003, 0x0009, 0x0000, 0x001f,
        0x0021, 0x0028, 0x002a, 0xffff, 0x007c, 0x0000, 0x0006, 0x0047,
        0x0000, 0xfff4, 0x007c, 0x0000, 0x0003, 0x004e, 0x0000, 0x0003,
        0x0029, 0x0001, 0x00a5, 0x007c, 0x0000, 0x004e, 0x0028, 0x0003,
        0x0003, 0x007c, 0x0000, 0x0045, 0x0028, 0x0004, 0x0003, 0x007c,
        0x0000, 0x0009, 0x0041, 0x0003, 0x0020, 0x0077, 0x0077, 0x0077,
        0x007c, 0x0000, 0x0009, 0x0041, 0x0003, 0x0017, 0x0077, 0x0065,
        0x0062, 0x007c, 0x0000, 0x0008, 0x0041, 0x0002, 0x000e, 0x0077,
        0x0033, 0x007c, 0x0000, 0x0009, 0x0041, 0x0003, 0x0006, 0x0066,
        0x0074, 0x0070, 0x0029, 0x0004, 0x0003, 0x0041, 0x0001, 0x0004,
        0x002e, 0x005b, 0x0003, 0x0009, 0x0000, 0x001f, 0x0021, 0x0028,
        0x002a, 0xffff, 0x007c, 0x0000, 0x0006, 0x0047, 0x0000, 0xfff4,
        0x007c, 0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 0x0029, 0x0003,
        0x0057, 0x007c, 0x0000, 0x0054, 0x0028, 0x0005, 0x0003, 0x007c,
        0x0000, 0x004b, 0x0028, 0x0006, 0x0003, 0x007c, 0x0000, 0x000c,
        0x0041, 0x0006, 0x0009, 0x006d, 0x0061, 0x0069, 0x006c, 0x0074,
        0x006f, 0x0029, 0x0006, 0x0003, 0x0041, 0x0001, 0x0004, 0x003a,
        0x005b, 0x0003, 0x0009, 0x0000, 0x001f, 0x0021, 0x003f, 0x0041,
        0xffff, 0x007c, 0x0000, 0x0006, 0x0047, 0x0000, 0xfff4, 0x007c,
        0x0000, 0x0003, 0x004e, 0x0000, 0x0003, 0x0041, 0x0001, 0x0004,
        0x0040, 0x005b, 0x0005, 0x000d, 0x0061, 0x007a, 0x0041, 0x005a,
        0x0030, 0x0039, 0x002d, 0x002d, 0x002e, 0x002e, 0x007c, 0x0000,
        0x0006, 0x0047, 0x0000, 0xfff0, 0x007c, 0x0000, 0x0003, 0x004e,
        0x0000, 0x0003, 0x0029, 0x0005, 0x0003, 0x0045, 0x0000, 0x0000, 
    };
 
    // the replace map maps strings to special strings in HTML, it simply
    // replaces all found strings matching the first column with the
    // strings of the second column
    private static String replaceMap[][] = {{"&", "&amp;"}, // replace & first
                                            {"ö", "&ouml;"},
                                            {"Ö", "&Ouml;"},
                                            {"ä", "&auml;"},
                                            {"Ä", "&Auml;"},
                                            {"ü", "&uuml;"},
                                            {"Ü", "&Uuml;"},
                                            {"ß", "&szlig;"},
                                            {"<", "&lt;"},
                                            {">", "&gt;"},
                                            {"(c)", "&copy;"},
                                            {"(C)", "&copy;"},
                                            {"\n", "<br>"}};
 
    public String filter(String input) throws StringFilterException
    {
        String retval = input;
  
        if(retval != null)
        {
            // replace all strings in replaceMap with the acording entity
            for(int i = 0; i < replaceMap.length; i++)
            {
                int fpos;
                int pos=0;
                // as long there are still unmapped string in the string
                while((fpos = retval.indexOf(replaceMap[i][0], pos)) != -1)
                {
                    // create the new string and copy the part of retval to
                    // it until the position of the string which should be
                    // replaced
                    String newstring = retval.substring(0, fpos);
                    // add the replacement
                    newstring += replaceMap[i][1];
                    // the new position where the go on with searching
                    pos = newstring.length();
                    // add the rest of retval to the new string
                    newstring += retval.substring(
                        fpos + replaceMap[i][0].length(),
                        retval.length()
                    );
                    // retval is the new string now
                    retval = newstring;
                }
            }
   
            RE url = new RE(new REProgram(URLDetectorPatternInstructions));
   
            int i = 0;
            while(url.match(retval, i))
            {
                // as long one pattern is found
                String paren = null;
                if((paren = url.getParen(1)) != null)
                {
                    // check if http:// https:// or ftp:// was found
                    String newstring = 
                        retval.substring(0, url.getParenStart(1));
                    newstring += "<a href=\"" + paren + "\">" 
                               + paren + "</a>";
                    i = newstring.length();
                    newstring += retval.substring(
                        url.getParenEnd(1),
                        retval.length()
                    );
                    retval = newstring;
                }
                else if((paren = url.getParen(3)) != null)
                {
                    // get protcoll indicator too
                    String type = url.getParen(4);
     
                    if((type != null) && (type.equals("ftp")))
                    {
                        // the hostname is ftp so extend with ftp://
                        String newstring = 
                            retval.substring(0, url.getParenStart(3));
                        newstring += "<a href=\"ftp://" + paren + "\">" 
                                   + paren + "</a>";
                        i = newstring.length();
                        newstring += retval.substring(
                            url.getParenEnd(3),
                            retval.length()
                        );
                        retval = newstring;
                    }
                    else
                    {
                        // the hostname is www,web,w3 so extend with
                        // http://
                        String newstring = 
                            retval.substring(0, url.getParenStart(3));
                        newstring += "<a href=\"http://" + paren + "\">"
                                   + paren + "</a>";
                        i = newstring.length();
                        newstring += retval.substring(
                            url.getParenEnd(3),
                            retval.length()
                        );
                        retval = newstring;
                    } 
                }
                else if((paren = url.getParen(5)) != null)
                {
                    // mailto - protocoll indicator found
                    String newstring = 
                        retval.substring(0, url.getParenStart(5));
                    newstring += "<a href=\"" + paren + "\">" 
                               + paren + "</a>";
                    i = newstring.length();
                    newstring += retval.substring(
                        url.getParenEnd(5), 
                        retval.length()
                    );
                    retval = newstring;
                }
            }
        }

        // return the modified string    
        return retval;
    }
}
  
