/* $Id: Base64.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * This class provides two static methods to encode and decode Base64
 * strings.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class Base64
{
    /**
     * decodes a Base64 encoded string and returns the result.
     *
     * @param encoded the Base64 encoded string
     * @return the decoded string
     */
    public static String decode(String encoded)
    {
        // the buffer where the decoded string is stored
        StringBuffer sb = new StringBuffer();
     
        // number of chars to check
        int maxturns;
     
        // find out how many loops usually the encoded string is filled up
        // with '=' and the check is not needed
        if((encoded.length() % 3) == 0)
        {
            maxturns = encoded.length();
        }
        else
        {
            // if string is not filled up with '=' check the fields as 0
            // see bottom 
            maxturns = encoded.length() + (3 - (encoded.length() % 3));
        }
     
        // does this char belongs to the decoded string ?
        boolean skip;
     
        //the decode buffer
        byte[] decode = new byte[4];

        // the actual byte
        byte b;
        int j = 0;
        for(int i = 0; i < maxturns; i++)
        {
            skip = false;
            //get the byte to convert or 0
            if(i < encoded.length())
                b = (byte) encoded.charAt(i);
            else
                // here are the not filled up chars are set to 0
                b = 0;
            
            // check capital letters
            if(b >= 65 && b < 91)
                decode[j] = (byte) (b - 65);
            // check lowercase letters
            else if(b >= 97 && b < 123)
                decode[j] = (byte) (b - 71);
            // check digits
            else if(b >= 48 && b < 58)
                decode[j] = (byte) (b + 4);
            // check '+'
            else if(b == '+')
                decode[j] = 62;
            // check '/'
            else if(b == '/')
                decode[j]=63;
            // if we find '=' the end of base64 code is reached
            else if(b == '=')
                decode[j]=0;
            else
            {
                char c = (char) b;
                // skip whitespaces, newlines and 0 added to the end of the
                // string
                if(c == '\n' || c == '\r' || c == ' ' 
                    || c == '\t' || b == 0)
                {
                    skip = true;
                }
                else
                {
                    // if there are any other chars than the parsed there
                    // is a bug in base64 encoding [ throw exception here
                    // or return null ]
                    System.out.println("No Base64 encoded string");
                }
            }
            // increment field counter for temporary field
            j++;
            // if the array of 4 bytes is filles up convert into 4 byte
            // word
            if(!skip && j == 4)
            {
                //shift the 6 bit bytes into a single 4 octet word
                int res = (decode[0] << 18) + (decode[1] << 12) 
                        + (decode[2] << 6) + decode[3];
                byte c;
                int k = 16;
                // extract chars form the word
                while(k >= 0)
                {
                    // 1: shift by 16 bit
                    // 2: shift by 8 bit
                    // 3: shift by 0 bit
                    /*
                     *  | | | | | | | | | | | | | | | | | | | | | | | | |
                     *  ^               ^               ^               ^
                     *       1. Byte         2. Byte        3. Byte
                     */
                    c = (byte) (res >> k);
                    if(c > 0)
                        sb.append((char) c);
                    k -= 8;
                }
                // reset j and the decode buffer
                j = 0;
                decode[0] = 0;
                decode[1] = 0;
                decode[2] = 0;
                decode[3] = 0;
            }
        }
        return sb.toString();
    }
    
    /**
     * encodes any string &lt;= 76 to a Base64 string and returns the
     * result as a string.
     *
     * @param plain the plaintext string
     * @return the base64 encoded equivalent to plaintext
     */
    public static String encode(String plain)
    {
        String retval = null;
        if(!(plain.length() > 76))
        {
            int maxturns;
            // the stringbuffer to held the result
            StringBuffer sb = new StringBuffer();
            //the encode buffer
            byte[] enc = new byte[3];
            // end of string reached
            boolean end = false;
            int j = 0;
            int i = 0;
            while(!end)
            {
                if(i == (plain.length() - 1))
                    end = true;
       
                enc[j] = (byte) plain.charAt(i);
                j++;
                if(j == 3 || end)
                {
                    int res = 0;
                    // first shift all bytes in one a 4 byte word
                    res = (enc[0] << 16) + (enc[1] << 8) + enc[2];
                    int b;
                    int lowestbit = 18 - (j * 6);
                    for(int toshift = 18; toshift >= lowestbit; 
                        toshift -= 6)
                    {
                        // shift the 3 8 Bit bytes into 4 6 Bit chars
                        b = res >>> toshift;
                        // masquerade the lower 6 bits only set the bits
                        // above to 0
                        b &= 63;
                        // convert the first 26 combinations to capital
                        // chars
                        if(b >= 0 && b < 26)
                            sb.append((char) (b + 65));
                        // convert the next 26 to lowercase chars
                        else if(b >= 26 && b < 52)
                            sb.append((char) (b + 71));
                        // covert the next 10 combinations to digits
                        else if(b >= 52 && b < 62)
                            sb.append((char) (b - 4));
                        else if(b == 62)
                            sb.append('+');
                        else if(b == 63)
                            sb.append('/');
                        // if line is longer than 76 chars go on in next
                        // line
                        if(sb.length() % 76 == 0)
                            sb.append('\n');
                    }
                    //now set the end chars to be pad character if there
                    //was less than integral input (ie: less than 24 bits)
                    if(end)
                    {
                        if(j == 1)
                            sb.append("==");
                        if(j == 2)
                            sb.append('=');
                    }
                    enc[0] = 0; 
                    enc[1] = 0; 
                    enc[2] = 0;
                    j = 0;
                }
                i++;
            }
            retval = sb.toString();
        }
        return retval;
    }
}

