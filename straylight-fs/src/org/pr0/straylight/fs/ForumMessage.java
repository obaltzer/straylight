/* $Id: ForumMessage.java,v 1.1 2001/07/04 04:07:46 racon Exp $ */

package org.pr0.straylight.fs;

import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fw.util.SimpleDate;

/**
 * A ForumMessage is a message belonging to a particular forum. It is used
 * to build lists of message via the ForumMessageList class. It contains
 * all necessary information about a message to generate a context object
 * which can transfer the data to the template.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2001/07/04 04:07:46 $
 */
class ForumMessage
{
    // the user which is the author if this message
    private User author;
    // do this message has replies
    private boolean hasReplies;
    // in which reply level is this message located
    private int replylevel;
    // to which thread this message belongs
    private int thread;
    // to which message this message is the reply
    private int replyto;
    // the subject of the message
    private String subject;
    // the body of the message
    private String body;
    // the creation date
    private SimpleDate createDate=null;
    // the message id
    private int id;
 
    /**
     * the contructor for messages where only the subject is needed and an
     * author is specified for the message. Messages where only the subject
     * are interessting in list of message like generated in the
     * ShowMessageListRequest.
     * 
     * @param id the messageid
     * @param subject the subject of the message
     * @param author the User object which specifies the author
     * @param date the date when the messages was created
     * @param thread the message Id of the root message of the thread this
     *               message belongs to
     * @param replylevel the level of replies this messages belongs to
     * @param replyto if this message is not a thread root the message Id of
     *                the message to which it is a reply
     * @param hasreplies true if this message has replies
     */
    protected ForumMessage(int id, String subject, User author,
                           SimpleDate date, int thread, int replylevel,
                           int replyto, boolean hasreplies)
    {
        this.id = id;
        this.author = author;
        this.subject = subject;
        this.createDate = date;
        this.hasReplies = hasreplies;
        this.replylevel = replylevel;
        this.thread = thread;
        this.replyto = replyto;
        this.body = null;
    }

    /**
     * the contructor for messages where only the subject is needed but no
     * author specified for the message. Messages where only the subject are
     * interessting in list of message like generated in the
     * ShowMessageListRequest.
     * 
     * @param id the messageid
     * @param subject the subject of the message
     * @param date the date when the messages was created
     * @param thread the message Id of the root message of the thread this
     *               message belongs to
     * @param replylevel the level of replies this messages belongs to
     * @param replyto if this message is not a thread root the message Id of
     *                the message to which it is a reply
     * @param hasreplies true if this message has replies
     */
    protected ForumMessage(int id, String subject, 
                           SimpleDate date, int thread, int replylevel,
                           int replyto, boolean hasreplies)
    {
        this.id = id;
        this.author = null;
        this.subject = subject;
        this.createDate = date;
        this.hasReplies = hasreplies;
        this.replylevel = replylevel;
        this.thread = thread;
        this.replyto = replyto;
        this.body = null;
    }

    /**
     * the constructor for the whole representation of a message including the
     * body of the message.
     *
     * @param id the message id
     * @param subject the subject of the message
     * @param body the body of the message
     * @param author the User object of the author
     * @param date the creation date of the message
     * @param thread the message Id of the root message of the thread this
     *               message belongs to
     * @param replylevel the level of replies this messages belongs to
     * @param replyto if this message is not a thread root the message Id of
     *                the message to which it is a reply
     * @param hasreplies true if this message has replies
     */
    protected ForumMessage(int id, String subject, String body,
                           User author, SimpleDate date, 
                           int thread, int replylevel, 
                           int replyto, boolean hasreplies)
    {
        this.id = id;
        this.author = author;
        this.subject = subject;
        this.body = body;
        this.createDate = date;
        this.hasReplies = hasreplies;
        this.replylevel = replylevel;
        this.thread = thread;
        this.replyto = replyto;
    }
  
    /**
     * the constructor for the whole representation of a message including the
     * body of the message but no author is specified.
     *
     * @param id the message id
     * @param subject the subject of the message
     * @param body the body of the message
     * @param date the creation date of the message
     * @param thread the message Id of the root message of the thread this
     *               message belongs to
     * @param replylevel the level of replies this messages belongs to
     * @param replyto if this message is not a thread root the message Id of
     *                the message to which it is a reply
     * @param hasreplies true if this message has replies
     */
    protected ForumMessage(int id, String subject, String body,
                           SimpleDate date, int thread, int replylevel, 
                           int replyto, boolean hasreplies)
    {
        this.id = id;
        this.author = null;
        this.subject = subject;
        this.body = body;
        this.createDate = date;
        this.hasReplies = hasreplies;
        this.replylevel = replylevel;
        this.thread = thread;
        this.replyto = replyto;
    }
        
    /**
     * returns the ID of the message.
     */
    protected int getId()
    {
        return id;
    }

    /**
     * returns the subject of the message.
     */
    protected String getSubject()
    {
        return subject;
    }

    /**
     * returns the body of the message.
     */
    protected String getBody()
    {
        return body;
    }

    /**
     * returns true if the message hast replies otherwise false.
     */
    protected boolean hasReplies()
    {
        return hasReplies;
    }

    /**
     * returns the Date when the message was created.
     */
    protected SimpleDate getDate()
    {
        return createDate;
    }

    /**
     * returns the User object of the author.
     */
    protected User getAuthor()
    {
        return author;
    }

    /**
     * returns the reply level of the message.
     */
    protected int getReplyLevel()
    {
        return replylevel;
    }

    /**
     * returns the ID of the thread root message.
     */
    protected int getThread()
    {
        return thread;
    }

    /**
     * returns the Id of the message this message is a reply to.
     */
    protected int getReplyTo()
    {
        return replyto;
    }
}
