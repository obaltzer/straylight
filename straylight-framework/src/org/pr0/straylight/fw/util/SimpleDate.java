/* $Id: SimpleDate.java,v 1.2 2001/09/27 16:39:58 racon Exp $ */

package org.pr0.straylight.fw.util;

/**
 * This is a small and simple class represents date/time. The classes used
 * in the Java 1.3 API are a bit to big and also to slow, so here is a very
 * simple one. But this class will not check any legal dates.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:58 $
 */
public class SimpleDate
{
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int sec;

    /**
     * initalize the object with a string in timestamp format
     * (YYYYMMDDHHMMSS) and determines the values from it.
     *
     * @param timestamp the timestamp string
     */
    public SimpleDate(String timestamp)
    {
        if(timestamp != null && timestamp.length() == 14)
        {
            try
            {
                year = Integer.parseInt(timestamp.substring(0, 4));
            }
            catch(NumberFormatException e)
            {
                year = 0;
            }
            try
            {
                month = Integer.parseInt(timestamp.substring(4, 6));
                if(month < 1 || month > 12) 
                    month = 0;
            }
            catch(NumberFormatException e)
            {
                month = 0;
            }
            try
            {
                day = Integer.parseInt(timestamp.substring(6, 8));
                if(day < 1 || day > 31) 
                    day = 0;
            }
            catch(NumberFormatException e)
            {
                day = 0;
            }
            try
            {
                hour = Integer.parseInt(timestamp.substring(8, 10));
                if(hour < 0 || hour > 23) 
                    hour = 0;
            }
            catch(NumberFormatException e)
            {
                hour = 0;
            }
            try
            {
                min = Integer.parseInt(timestamp.substring(10, 12));
                if(min < 0 || min > 59) 
                    min = 0;
            }
            catch(NumberFormatException e)
            {
                min = 0;
            } 
            try
            {
                sec = Integer.parseInt(timestamp.substring(12, 14));
                if(sec < 0 || sec > 59) 
                    sec = 0;
            }
            catch(NumberFormatException e)
            {
                sec = 0;
            }
        }
        else
        {
            year = month = day = hour = min = sec = 0;
        } 
    } 
    
    public int getYear()
    {
        return year;
    }

    public int getMonth()
    {
        return month;
    }

    public int getDay()
    {
        return day;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMin()
    {
        return min;
    }

    public int getSec()
    {
        return sec;
    }
}
