/* $Id: ContextForum.java,v 1.2 2001/09/27 16:39:52 racon Exp $ */

package org.pr0.straylight.fs.context;

/**
 * The forum representation class in the Velocity context.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a> 
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:52 $
 */
public class ContextForum
{
    private String title = "";
    private String description = "";
    private String homepage = "";
    private String base = "";
    private String newmessages = "";
 
    /**
     * Contructor sets all necessary values.
     *
     * @param title the title of the forum
     * @param description the description of the forum
     * @param homepage the URL of the forum homepage
     * @param base the base URL to access the forum
     * @param newmessages the number of messages posted in the last 24
     *                    hours
     */
    public ContextForum(String title, String description, String homepage,
                        String base, String newmessages)
    {
        this.title = title;
        this.description = description;
        this.homepage = homepage;
        this.base = base;
        this.newmessages = newmessages;
    }
 
    /**
     * Returns the title of the forum to the Template.
     */
    public String getTitle()
    {
        return title;
    }
 
    /**
     * Returns the description of the forum to the template.
     */
    public String getDescription()
    {
        return description;
    }
 
    /**
     * Returns the base URL.
     */
    public String getBase()
    {
        return base;
    }

    /**
     * Returns the homepage URL.
     */
    public String getHomepage()
    {
        return homepage;
    }

    /**
     * returns the number of new messages posted in the last 24 hours.
     */
    public String getNewMessages()
    {
        return newmessages;
    }
} 
