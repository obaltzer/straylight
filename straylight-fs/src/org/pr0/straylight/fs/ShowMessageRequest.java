/* $Id: ShowMessageRequest.java,v 1.3 2001/09/27 16:39:50 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fw.um.UserNotFoundException;
import org.pr0.straylight.fw.um.UserManagerException;
import org.pr0.straylight.fw.util.SimpleDate;
import org.pr0.straylight.fw.util.StringFilterChain;
import org.pr0.straylight.fw.util.TextToHTMLStringFilter;
import org.pr0.straylight.fw.util.StringFilterException;
import org.pr0.straylight.fs.context.ContextMessage;
import org.pr0.straylight.fs.context.ContextMessageList;
import org.pr0.straylight.fs.context.ContextForum;
import org.pr0.straylight.fs.context.ContextAuthor;
import org.pr0.straylight.fs.context.ContextDate;

/**
 * The ShowMessageRequest generates a context for displaying a
 * message.
 * It fetches the content of the message from the database and also
 * provides a thread view for the thread the message belongs to.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:50 $
 */
class ShowMessageRequest implements RequestHandler
{
    /**
     * the context which will be generated.
     */
    private Hashtable context;

    /**
     * the name of the response template.
     */
    private String templatename;

    /**
     * the logging category
     */
    private static Category logger = null;

    /**
     * string filter to extend the text w/ HTML tags.
     */
    private static StringFilterChain outgoingFilter = null;

    /**
     * handles the /forumname/showMessage request. It expects a valid
     * HttpServletRequest object and a Forum object the request is assigned
     * to.
     *
     * @param request the HttpServletRequest object which provides more
     *                information about the request.
     * @param forum the forum object to which the message belongs
     * @throws ForumServletException on any fatal error
     */
    protected ShowMessageRequest(HttpServletRequest request,
                                 Forum forum)
                                 throws ForumServletException
    {
        // initialize logger for this class
        if(logger == null)
            this.logger = Category.getInstance(this.getClass().getName());

        // initialze string filter
        if(outgoingFilter == null)
        {
            outgoingFilter = new StringFilterChain();
            outgoingFilter.add(new TextToHTMLStringFilter());
        }
  
        // the application BaseURL
        String baseurl = request.getContextPath() 
                       + request.getServletPath();
  
        // the forum BaseURL
        String forumbase = baseurl + "/" + forum.getShortname();
  
        // get the request parameter
        // the message id
        String messageVal = request.getParameter("message");
  
        // the back jump address to the forum message index
        String backUrl = request.getParameter("back");

        // the flag, indicates that the thread of the message should be shown
        String threadviewVal = request.getParameter("threadview");

        // the ID of the message which should be shown
        int messageid;
  
        // get the message id
        if(messageVal != null)
        {
            try
            {
                messageid = Integer.parseInt(messageVal);
            }
            catch(NumberFormatException e)
            {
                logger.error("[" + request.getRemoteAddr() + "]: "
                            + "Cannot display message: "
                            + "wrong parameter format.");
                throw new ForumServletException("Wrong Parameter",
                                                "Cannot display "
                                              + "message because your "
                                              + "request provided a "
                                              + "parameter in "
                                              + "the wrong format.");
            }
        }
        else
        {
            logger.error("[" + request.getRemoteAddr() + "]: "
                        + "Cannot display message: "
                        + "call without a message ID.");
            throw new ForumServletException("Wrong Parameter",
                                            "Cannot display message "
                                          + "because your request does "
                                          + "not tell which message "
                                          + "should be shown.");
        }
  
        String backenc = "";
  
        // get the back jump url
        if(backUrl != null)
        {
            backenc = "&back=" + backUrl;
            backUrl = backUrl.replace('$', '&');
        }
        else
        {
            backUrl = "";
        }

        // check if thread view is enable
        boolean threadview = false;
        
        if(threadviewVal != null)
        {
            if((threadviewVal.length() == 1) 
              && (threadviewVal.charAt(0) == '1'))
            {
                threadview = true;
            }
        }
        
  
        // start processing
        // prepare the SQL statement first
        String statement = "SELECT ForumMessage.Subject, "
                         + "ForumMessage.Body, "
                         + "ForumMessage.AuthorId, "
                         + "ForumMessage.Thread, "
                         + "ForumMessage.CreateDate "
                         + "FROM ForumMessage WHERE "
                         + "ForumMessage.ForumId = " + forum.getId()
                         + " AND ForumMessage.Id = " + messageid 
                         + " LIMIT 0,1";
        
        // the context message
        ContextMessage contextmessage = null;
        ContextMessageList contextmessagelist = null;
      
        // get the database connection
        DBConnection dbcon = null;
        try
        {
            dbcon = ForumServlet.dbpool.getConnection();
            ResultSet rs = dbcon.executeQuery(statement);
            if(rs.next())
            {
                // ok a message with this id is in the database
                ContextAuthor contextauthor = null;
                if(forum.isPostRestricted())
                {
                    // if the forum is postrestricted than there should be
                    // an author available for this message
                    try
                    {
                        // get the author from the user manager
                        User author =
                            ForumServlet.um.getUser(rs.getInt(3));
                        if(author != null)
                        {
                            if(author.isPublicEMail())
                            {
                                // put the address in the public context
                                // too
                                contextauthor = new ContextAuthor(
                                    author.getSurname(),
                                    author.getFirstname(),
                                    author.getEMail()
                                );
                            }
                            else
                            {
                                // do not put the address in the 
                                // public context
                                contextauthor = new ContextAuthor(
                                    author.getSurname(),
                                    author.getFirstname(),
                                    ""
                                );
                            }
                        }
                        // else create empty authorcontext object
                        else contextauthor =
                            new ContextAuthor("", "", "");
                    }
                    catch(UserNotFoundException e)
                    {
                        logger.warn("[" + request.getRemoteAddr() + "]: "
                                    + "Author with User-Id " 
                                    + rs.getInt(3) 
                                    + " not found.");
                    }
                    catch(UserManagerException e)
                    {
                        logger.error("[" + request.getRemoteAddr() + "]: "
                                     + "UserManager is not working. " 
                                     + "Exception: "
                                     + e.getMessage());
                    }
                }
                // else create empty authorcontext object
                else contextauthor = new ContextAuthor("", "", "");
            
                // get the creation date of the message
                SimpleDate date = new SimpleDate(rs.getString(5));
                ContextDate contextdate = new ContextDate(
                    date.getDay(),
                    date.getMonth(),
                    date.getYear(),
                    date.getHour(),
                    date.getMin(),
                    date.getSec()
                );

                // get subject and body
     
                String subject = rs.getString(1);
                String body = rs.getString(2);

                // filter the strings
                try
                {
                    subject = outgoingFilter.filter(subject);
                    body = outgoingFilter.filter(body);
                }
                catch(StringFilterException e)
                {
                    logger.warn("[" + request.getRemoteAddr() + "]: "
                                + "Subject or body of message " 
                                + messageid + " are incorrect "
                                + "strings.");
                }
                
                String accessurl = forumbase + "/showMessage?message=" 
                                 + messageid + backenc;
                
                String replyurl = forumbase + "/newReply?message="
                                + messageid + backenc;
                                
                contextmessage = new ContextMessage(
                    contextauthor,      // the author context
                    subject,            // the subject
                    body,               // the body
                    accessurl,          // the accessurl
                    replyurl,           // the reply url
                    null,               // the reply level
                    false,               // has replies
                    contextdate,        // the date context
                    true,               // is expanded
                    null                // toggle expand url
                );
               
                
                // get the thread view if requested
                ForumMessageList threadlist = null; 
                if(threadview)
                {
                    int thread;
                    
                    if(rs.getInt(4) != 0)
                        thread = rs.getInt(4);
                    else
                        thread = messageid;
                    
                    try
                    {
                        
                        threadlist = 
                            new ForumMessageList(forum, thread, dbcon,
                                                 ForumServlet.um);
                        
                    }
                    catch(ForumMessageListException e)
                    {
                        logger.error("[" + request.getRemoteAddr() + "]: "
                                     + "Cannot get threadview for thread "
                                     + thread + ".");
                    }
                }
                
                // generate the message list context
                if(threadlist != null)
                {
                    contextmessagelist = new ContextMessageList();
                    
                    for(int i = 0; i < threadlist.size(); i++)
                    {
                        ForumMessage tmessage = 
                            (ForumMessage)threadlist.get(i);
                        User tauthor = tmessage.getAuthor();
                        ContextAuthor contexttauthor = null;
                        if(tauthor != null)
                        {
                            if(tauthor.isPublicEMail())
                            {
                                // put the address in the public context
                                contexttauthor = new ContextAuthor(
                                    tauthor.getSurname(),
                                    tauthor.getFirstname(),
                                    tauthor.getEMail()
                                );
                            }
                            else
                            {
                                // don't put the authors eMail-address in
                                contexttauthor = new ContextAuthor(
                                    tauthor.getSurname(),
                                    tauthor.getFirstname(),
                                    ""
                                );
                            }
                        }
                        else
                        {
                            contexttauthor=new ContextAuthor("", "", "");
                        }
                        
                        String tsubject = tmessage.getSubject();
                        // filter the subject
                        try
                        {
                            tsubject = outgoingFilter.filter(tsubject);
                        }
                        catch(StringFilterException e)
                        {
                            logger.warn("[" + request.getRemoteAddr() 
                                        + "]: "
                                        + "Subject or body of message " 
                                        + tmessage.getId() 
                                        + " are incorrect "
                                        + "strings.");
                        }
       
                        String taccessurl = 
                            forumbase + "/showMessage?message="
                            + tmessage.getId()
                            + (threadview ? "&threadview=1" : "")
                            + backenc;
       
                        SimpleDate tdate = tmessage.getDate();
       
                        ContextDate contexttdate = new ContextDate(
                            tdate.getDay(),
                            tdate.getMonth(),
                            tdate.getYear(),
                            tdate.getHour(),
                            tdate.getMin(),
                            tdate.getSec()
                        );
                                    
                        contextmessagelist.add(
                            new ContextMessage(
                                contexttauthor,
                                tsubject,
                                null,
                                taccessurl,
                                null,
                                new Integer(tmessage.getReplyLevel()),
                                tmessage.hasReplies(),
                                contexttdate,
                                true,
                                null
                            ) 
                        );
                    } // end of for
                } // end of test != null 
            }  
            else
            {
                if(dbcon != null)
                    ForumServlet.dbpool.releaseConnection(dbcon);
                dbcon = null;
                
                logger.error("["+request.getRemoteAddr()+"]: "
                             + "Requested message '" + messageid 
                             + "' not found in forum.");
                throw new ForumServletException(
                    "Message not found",
                    "Your requested message '" + messageid 
                    + "' could not be "
                    +"found in current forum."
                );
            }
            rs.close();

            // important !!!
            // release connection
            ForumServlet.dbpool.releaseConnection(dbcon);
        }
        catch(DBConnectionPoolException e)
        {
            logger.error("["+request.getRemoteAddr()+"]: "
                         + "DBConnectionPool Exception: " 
                         + e.getMessage());
            
            throw new ForumServletException(
                "Database Connection Pool Error",
                "Cannot get free database connection from database "
                + "connection pool."
            );
        }
        catch(SQLException e)
        {
            if(dbcon!=null) 
                ForumServlet.dbpool.releaseConnection(dbcon);
            
            logger.error("["+request.getRemoteAddr()+"]: "
                         + "Database access error: " + e.getMessage());
            
            throw new ForumServletException("Database access error",
                                            "Cannot process database "
                                            + "access.");
        }
        ContextForum contextforum = new ContextForum(
            forum.getTitle(),
            forum.getDescription(),
            forum.getHomepage(),
            forumbase,
            null
        );
        
        String threadviewtoggle = forumbase + "/showMessage?message="
                                + messageid
                                + (threadview ? "" : "&threadview=1")
                                + backenc;
        
        context=new Hashtable();
        context.put("BaseURL",baseurl);
        context.put("Forum",contextforum);
        if(contextmessagelist != null)
            context.put("ThreadList", contextmessagelist);
        context.put("Message", contextmessage);
        context.put("BackLink", backUrl);
        context.put("ThreadView", threadviewtoggle);
        templatename="ShowMessage";
    }

    public Hashtable getContext()
    {
        return context;
    }

    public String getTemplateName()
    {
        return templatename;
    }
}
