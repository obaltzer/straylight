/* $Id: ContextMessage.java,v 1.3 2001/09/27 16:39:52 racon Exp $ */

package org.pr0.straylight.fs.context;

/**
 * The ContextMessage is the representation of a forum message in the
 * Velocity context.
 * 
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:52 $
 */
public class ContextMessage
{
    ContextAuthor author = null;
    String subject = "";
    String body = "";
    Integer replylevel = null;
    Integer hasreply = null;
    ContextDate postingdate = null;
    String accessurl = "";
    String replyurl = "";
    String toggleexpandurl = "";
    boolean expanded;
 
    /**
     * The constructor sets the needed values.
     *
     * @param accessurl the URL which calls the showMessage commnd for this
     *                  message
     * @param author the ContextAuthor object of the message
     * @param subject the subject of the message
     * @param body the body of the message
     * @param replylevel the level of replies the message belongs to
     * @param hasreply indicates if the message has replies
     * @param postingdate the ContextDate object
     */
    public ContextMessage(ContextAuthor author, String subject, 
                          String body, String accessurl,
                          String replyurl,
                          Integer replylevel, boolean hasreply, 
                          ContextDate postingdate, boolean expanded, 
                          String toggleExpandURL)
    {
        this.author = author;
        this.subject = subject;
        this.body = body;
        this.replylevel = replylevel;
        this.hasreply = new Integer(hasreply == true ? 1 : 0);
        this.postingdate = postingdate;
        this.accessurl = accessurl;
        this.replyurl = replyurl;
        this.toggleexpandurl = toggleExpandURL;
        this.expanded = expanded;
    }
 
    public ContextAuthor getAuthor()
    {
        return author;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getBody()
    {
        return body;
    }

    public Integer getReplyLevel()
    {
        return replylevel;
    }

    public Integer getHasReply()
    {
        return hasreply;
    }

    public ContextDate getPostingDate()
    {
        return postingdate;
    }

    public String getAccessURL()
    {
        return accessurl;
    }
    
    public String getReplyURL()
    {
        return replyurl;
    }

    public String getToggleExpandURL()
    {
        return toggleexpandurl;
    }

    public Integer getIsExpanded()
    {
        return expanded ? new Integer(1) : new Integer(0);
    }
}
