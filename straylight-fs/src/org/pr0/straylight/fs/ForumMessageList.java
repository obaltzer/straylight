/* $Id: ForumMessageList.java,v 1.4 2001/09/27 16:39:50 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.um.UserManager;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fw.um.UserManagerException;
import org.pr0.straylight.fw.um.UserNotFoundException;
import org.pr0.straylight.fw.util.ArrayUtils;
import org.pr0.straylight.fw.util.StringUtils;
import org.pr0.straylight.fw.util.SimpleDate;

/**
 * This class provides an implementation of a list of ForumMessages. It
 * extends the <code>java.util.Vector</code> class by adding function to
 * access the database and generate message lists from the content of the
 * database. This class is usualy in use by the the ShowMessageList request
 * handler and the ShowMessage request handler.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.4 $ $Date: 2001/09/27 16:39:50 $
 */
class ForumMessageList extends Vector
{
    // some available consts
    /**
     * sort the list by date.
     */
    protected static final int BYDATE = 1;

    /**
     * sort the list by subject.
     */
    protected static final int BYSUBJECT = 2;
    
    /**
     * the local static logger.
     */
    private static Category logger = null;
    
    /**
     * a local reference to the current forum the list is generated for.
     */
    private Forum forum;

    /**
     * a local reference to the used user manager.
     */
    private UserManager um;
    
    /**
     * if true the authors should be fetched to.
     */
    private static boolean postrestricted = false;
    
    /**
     * generates a flat list of messages. It will start with the
     * <code>start</code> message in the specified <code>sort</code> and a
     * will put <code>count</code> messages in the right order into the
     * list for forum <code>forum</code>. It will access the database and
     * just get a limited number of messages from the database where the
     * database will sort the results.
     *
     * TODO implement sort by author
     *
     * @param forum the Forum object of the current forum the list should
     *              be generated for
     * @param start the first message in the list
     * @param count the number of message in the list
     * @param sort the sorting algorithm which should be used
     * @param reverse true if the list should be sorted in reverse order
     * @param dbcon the database connection which should be used
     * @param um the UserManager reponsible to retrieve users
     * @throws ForumMessageListException on any fatal erre like database
     *                                   access error
     */
    protected ForumMessageList(Forum forum, int start, int count,
                               int sort, boolean reverse, 
                               DBConnection dbcon, UserManager um)
                               throws ForumMessageListException
    {
        // initialize the java.util.Vector class with at least count, so we
        // do not need any resizing of the Vector
        super(count);
        
        // XXX some debug code
        long time = System.currentTimeMillis();

        // initalize the logger for this class if not already done
        if(logger == null)
            logger = Category.getInstance(this.getClass().getName());

        // make the forum object available
        this.forum = forum;
  
        // check if the forum is post restricted and whether authors should
        // be fetched
        postrestricted = forum.isPostRestricted();
        
        // construct the SQL statement for flat message list
        // just messages from the actual Forum are requested
        String where = "WHERE ForumMessage.ForumId=" + forum.getId()
                     + " ";
        
        // the order of sorting (reverse?)
        String order = "";
        // sort by date
        if(sort == BYDATE) 
            order = "ORDER BY ForumMessage.CreateDate ";
        // sort by subject
        else if(sort == BYSUBJECT) 
            order = "ORDER BY ForumMessage.Subject ";
        // sort direction
        if(reverse == true)
            order += "DESC ";
        else 
            order += "ASC ";
        
        // how many message should be fetched
        String limit = "LIMIT " + start + "," + count;
  
        // prepare SQL statement
        String statement = "SELECT Id,Subject,AuthorId,CreateDate "
                         + "FROM ForumMessage "
                         + where
                         + order
                         + limit;
  
        // execute the SQL statement
        try
        {
            ResultSet rs = dbcon.executeQuery(statement);
            while(rs.next())
            {
                User author = null;
                if(postrestricted)
                {
                    // if the forum is postrestricted than there should be
                    // an author available for this message
                    try
                    {
                        // get the author from the database
                        author = um.getUser(rs.getInt(3));
                    }
                    catch(UserNotFoundException e)
                    {
                        logger.warn("Author with User-Id " + rs.getInt(3) 
                                    + " not found.");
                    }
                    catch(UserManagerException e)
                    {
                        // if we cannot get the User object for the author
                        // because of the UserManager than ignore it for
                        // the list and just make a big log message
                        
                        author = null;
                        
                        logger.error("UserManager is not working while "
                                     + "getting author user. Exception: "
                                     + e.getMessage());
                    }
                }
                
                int messageid = rs.getInt(1);
                
                // create the message object and add it to the list
                // all messages in the simple list have no replies
                if(author != null)
                {
                    this.add(
                        new ForumMessage(messageid, rs.getString(2),
                                         author,
                                         new SimpleDate(rs.getString(4)),
                                         messageid, 0, -1, false)
                    );
                }
                else
                {
                    this.add(
                        new ForumMessage(rs.getInt(1), rs.getString(2),
                                         new SimpleDate(rs.getString(4)),
                                         messageid, 0, -1, false)
                    );
                }
            }
        } 
        catch(SQLException e)
        {
            // the current connection will be released by the calling
            // method
            logger.error("SQL Exception with statement: '"
                        + statement + "' SQLException: "
                        + e.getMessage());
            
            throw new ForumMessageListException();
        }

        // XXX debug
        logger.debug("Flat: " + (System.currentTimeMillis() - time));
    } 
    
    /**
     * generates a thread list of messages. It will start with the
     * <code>start</code> thread in the specified <code>sort</code> and a
     * will put <code>count</code> threads in the right order into the
     * list for forum <code>forum</code>. It will first fetch all thread
     * roots in the specified range and than the several expaned messages
     * according to the content of the <code>expand</code> array.
     *
     * TODO implement sort by author
     *
     * @param forum the Forum object of the current forum the list should
     *              be generated for
     * @param start the first message in the list
     * @param count the number of message in the list
     * @param sort the sorting algorithm which should be used
     * @param reverse true if the list should be sorted in reverse order
     * @param expand an array of message id which where the replies to the
     *               messages should be expanded
     * @param dbcon the database connection which should be used
     * @param um the UserManager to retrieve users
     * @throws ForumMessageListException on any fatal erre like database
     *                                   access error
     */
    protected ForumMessageList(Forum forum, int start, int count,
                               int sort, boolean reverse, int[] expand,
                               DBConnection dbcon, UserManager um)
                               throws ForumMessageListException
    {
        // initialize the java.util.Vector class with at least count, so we
        // can reduce the resizing of the Vector
        super(count);
        
        // XXX some debug code
        long time = System.currentTimeMillis();

        // initalize the logger for this class if not already done
        if(logger == null)
            logger = Category.getInstance(this.getClass().getName());

        // make the forum object available
        this.forum = forum;
  
        // check if the forum is post restricted and whether authors
        // should be fetched
        postrestricted = forum.isPostRestricted();
        
        // construct the SQL statement for flat message list
        // just messages from the actual Forum are requested
        String where = "WHERE ForumMessage.ForumId=" + forum.getId() 
                     + " AND ReplyTo IS NULL ";
        
        // the order of sorting (reverse?)
        String order = "";
        // sort by date
        if(sort == BYDATE) 
            order = "ORDER BY ForumMessage.CreateDate ";
        // sort by subject
        else if(sort == BYSUBJECT) 
            order = "ORDER BY ForumMessage.Subject ";
        // sort direction
        if(reverse == true)
            order += "DESC ";
        else 
            order += "ASC ";
        
        // how many message should be fetched
        String limit = "LIMIT " + start + "," + count;
  
        // prepare SQL statement
        String statement = "SELECT Id,Subject,AuthorId,HasReply, "
                         + "CreateDate "
                         + "FROM ForumMessage "
                         + where
                         + order
                         + limit;
        
        // lists the messages which have replies
        int[] withreply = {};
        // execute the SQL statement
        try
        {
            ResultSet rs = dbcon.executeQuery(statement);
            while(rs.next())
            {
                User author = null;
                if(postrestricted)
                {
                    // if the forum is postrestricted than there should be
                    // an author available for this message
                    try
                    {
                        // get the author from the database
                        author = um.getUser(rs.getInt(3));
                    }
                    catch(UserNotFoundException e)
                    {
                        logger.warn("Author user not found.");
                    }
                    catch(UserManagerException e)
                    {
                        // if we cannot get the User object for the author
                        // because of the UserManager than ignore it for
                        // the list and just make a big log message
                        
                        author = null;
                        
                        logger.error("UserManager is not working while "
                                     + "getting author user. Exception: "
                                     + e.getMessage());
                    }
                }
                
                int messageid = rs.getInt(1);
                boolean hasreplies = rs.getInt(4) == 1 ? true : false;
                
                // if this message has a reply put its id to the list
                if(hasreplies)
                    withreply = ArrayUtils.add(withreply, messageid);
                    
                // create the message object and add it to the list
                // all messages in the simple list have no replies
                if(author != null)
                {
                    this.add(
                        new ForumMessage(messageid, rs.getString(2),
                                         author,
                                         new SimpleDate(rs.getString(5)),
                                         messageid, 0, -1, hasreplies)
                    );
                }
                else
                {
                    this.add(
                        new ForumMessage(rs.getInt(1), rs.getString(2),
                                         new SimpleDate(rs.getString(5)),
                                         messageid, 0, -1, hasreplies)
                    );
                }
            }
            
            // close result set
            rs.close();
            
            // insert the replies to the list
            if(withreply.length > 0 && expand.length > 0)
                insertReplies(dbcon, um, expand, withreply, 1);
        } 
        catch(SQLException e)
        {
            // the current connection will be released by the calling
            // method
            logger.error("SQL Exception with statement: '"
                        + statement + "' SQLException: "
                        + e.getMessage());
            
            throw new ForumMessageListException();
        }
        // XXX debug
        logger.debug("Thread: " + (System.currentTimeMillis() - time));
    }
    
    /**
     * generates a thread view of the given thread. This constructor is
     * used by the ShowMessageRequest class and returns a list of all
     * messages in the given thread.
     *
     * @param forum the Forum object of the current forum the list should
     *              be generated for
     * @param thread the message-ID of the first message in the thread
     * @param dbcon database connection which should be used
     * @param um UserManager to retrieve user info
     * @throws ForumMessageListException on any fatal erre like database
     *                                   access error
     */
    protected ForumMessageList(Forum forum, int thread, 
                               DBConnection dbcon, UserManager um)
                               throws ForumMessageListException
    {
        // initialize the java.util.Vector class with at least count, so we
        // can reduce the resizing of the Vector
        super();
        
        // XXX some debug code
        long time = System.currentTimeMillis();

        // initalize the logger for this class if not already done
        if(logger == null)
            logger = Category.getInstance(this.getClass().getName());

        // make the forum object available
        this.forum = forum;
  
        // check if the forum is post restricted and whether authors
        // should be fetched
        postrestricted = forum.isPostRestricted();
        
        // construct the SQL statement for getting the thread leading
        // message
        String statement = "SELECT Subject,AuthorId,HasReply, "
                         + "CreateDate "
                         + "FROM ForumMessage "
                         + "WHERE ForumMessage.Id = " + thread
                         + " LIMIT 0,1";
        
        // true if the message has a reply
        boolean hasreplies = false;
        
        // execute the SQL statement
        try
        {
            ResultSet rs = dbcon.executeQuery(statement);
            if(rs.next())
            {
                User author = null;
                if(postrestricted)
                {
                    // if the forum is postrestricted than there should be
                    // an author available for this message
                    try
                    {
                        // get the author from the database
                        author = um.getUser(rs.getInt(2));
                    }
                    catch(UserNotFoundException e)
                    {
                        logger.warn("Author user not found.");
                    }
                    catch(UserManagerException e)
                    {
                        // if we cannot get the User object for the author
                        // because of the UserManager than ignore it for
                        // the list and just make a big log message
                        
                        author = null;
                        
                        logger.error("UserManager is not working while "
                                     + "getting author user. Exception: "
                                     + e.getMessage());
                    }
                }
                
                hasreplies = rs.getInt(3) == 1 ? true : false;
                
                // create the message object and add it to the list
                // all messages in the simple list have no replies
                if(author != null)
                {
                    this.add(
                        new ForumMessage(thread, rs.getString(1),
                                         author,
                                         new SimpleDate(rs.getString(4)),
                                         thread, 0, -1, hasreplies)
                    );
                }
                else
                {
                    this.add(
                        new ForumMessage(thread, rs.getString(1),
                                         new SimpleDate(rs.getString(4)),
                                         thread, 0, -1, hasreplies)
                    );
                }
            }
            
            // close result set
            rs.close();
            
            int[] withreply = new int[1];
            withreply[0] = thread;
            
            // insert the replies to the list
            if(hasreplies)
                insertReplies(dbcon, um, null, withreply, 1);
        } 
        catch(SQLException e)
        {
            // the current connection will be released by the calling
            // method
            logger.error("SQL Exception with statement: '"
                        + statement + "' SQLException: "
                        + e.getMessage());
            
            throw new ForumMessageListException();
        }
        // XXX debug
        logger.debug("Thread_Only: " + (System.currentTimeMillis() 
                     - time));
    }
    
    /**
     * inserts replies in the current list. It expects a list of message id
     * which should be expanded if they have a reply and a list of messages
     * which are in the current list and have replies. It will only insert
     * a reply to to the list if the parent message is in the list too.
     *
     * @param dbcon the database connection which should be used
     * @param um UserManager to retrieve User infos
     * @param expand the list of messages which should be expanded or
     *               'null' to expand all messages
     * @param withreply a list of messages in the current list which have
     *                  a reply
     * @param replylevel the reply level all replies in this call are
     * @throws ForumMessageListException on any fatal error like database
     *                                   access error
     */
    private void insertReplies(DBConnection dbcon, UserManager um,
                               int[] expand, int[] withReply, 
                               int replyLevel)
                               throws ForumMessageListException
    {
        Vector expanded = new Vector();

        // create the list of conditionals
        for(int i = 0; i < withReply.length; i++)
            if(expand == null 
                || ArrayUtils.indexOf(expand, withReply[i]) > -1)
            {
                expanded.add("ForumMessage.ReplyTo = " + withReply[i]);
            }
       
        String[] expandedarray = {};
        expandedarray = (String[])expanded.toArray(expandedarray);
        
        // create the SQL statement
        String statement = "SELECT Id,Subject,AuthorId,HasReply,ReplyTo, "
                         + "CreateDate "
                         + "FROM ForumMessage "
                         + "WHERE ForumMessage.ForumId = " + forum.getId()
                         + " AND ReplyLevel = " + replyLevel
                         + " AND (" 
                         + StringUtils.join(expandedarray, " OR ") + ")"
                         + " ORDER BY CreateDate DESC";
        
        // XXX DEBUG
        logger.debug("SQL Statement: " + statement);
        
        // collects messages with replies
        int[] l_withReply = {};
        
        try
        {
            ResultSet rs = dbcon.executeQuery(statement);
            while(rs.next())
            {
                User author = null;
                if(postrestricted)
                {
                    // if the forum is postrestricted than there should be
                    // an author available for this message
                    try
                    {
                        // get the author from the database
                        author = um.getUser(rs.getInt(3));
                    }
                    catch(UserNotFoundException e)
                    {
                        logger.warn("Author user not found.");
                    }
                    catch(UserManagerException e)
                    {
                        // if we cannot get the User object for the author
                        // because of the UserManager than ignore it for
                        // the list and just make a big log message
                        
                        author = null;
                        
                        logger.error("UserManager is not working while "
                                     + "getting author user. Exception: "
                                     + e.getMessage());
                    }
                }

                int messageId = rs.getInt(1);

                boolean hasReplies = rs.getInt(4) == 1 ? true : false;
                
                // if the message has replies add it to the local list of
                // message with replies in this reply level
                if(hasReplies)
                    l_withReply=ArrayUtils.add(l_withReply, messageId);

                int isReplyTo = rs.getInt(5);

                // find the position where to include the message
                int size = this.size();
                int insertAt = -1;
                int thread = -1;
                
                for(int i = 0; i < size; i++)
                {
                    ForumMessage parent = (ForumMessage)this.get(i);
                    
                    if(parent.getId() == isReplyTo)
                    {
                        insertAt = i + 1;
                        thread = parent.getThread();
                        break;
                    }
                }
                        
                if(insertAt != -1 )
                {
                    if(author != null)
                    {
                        this.add(insertAt,
                            new ForumMessage(
                                messageId, rs.getString(2),
                                author,
                                new SimpleDate(rs.getString(6)),
                                thread, replyLevel, isReplyTo,
                                hasReplies
                            )
                        );
                    }
                    else
                    {
                        this.add(insertAt,
                            new ForumMessage(
                                messageId, rs.getString(2),
                                new SimpleDate(rs.getString(6)),
                                thread, replyLevel, isReplyTo,
                                hasReplies
                            )
                        );
                   }
                }
                else
                {
                    logger.warn("Got reply, but don't know where to "
                                + " insert in list. Message Id: " 
                                + messageId);
                }
            }
            
            // call itselv recursive here are messages with replies
            if(l_withReply.length > 0)
                insertReplies(dbcon, um, expand, l_withReply, 
                              replyLevel + 1);

        }
        catch(SQLException e)
        {
            logger.error("Database access error while getting replies. "
                         + "SQLException: " + e.getMessage());

            throw new ForumMessageListException();
        }
    }
}
