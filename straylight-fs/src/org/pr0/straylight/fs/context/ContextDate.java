/* $Id: ContextDate.java,v 1.2 2001/09/27 16:39:52 racon Exp $ */

package org.pr0.straylight.fs.context;

/**
 * Represents a date/time in the Velocity context.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:52 $
 */
public class ContextDate
{
    private Integer hour = null;
    private Integer min = null;
    private Integer sec = null;
    private Integer day = null;
    private Integer month = null;
    private Integer year = null;
 
    public ContextDate(int day, int month, int year, int hour, 
                       int min, int sec)
    {
        this.hour = new Integer(hour);
        this.min = new Integer(min);
        this.sec = new Integer(sec);
        this.day = new Integer(day);
        this.month = new Integer(month);
        this.year = new Integer(year);
    }

    public Integer getYear()
    {
        return year;
    }

    public Integer getMonth()
    {
        return month;
    }

    public Integer getDay()
    {
        return day;
    }

    public Integer getHour()
    {
        return hour;
    }

    public Integer getMin()
    {
        return min;
    }

    public Integer getSec()
    {
        return sec;
    }
}
