/* $Id: NewReplyRequest.java,v 1.2 2002/02/10 18:54:22 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.util.SimpleDate;
import org.pr0.straylight.fw.util.StringFilterChain;
import org.pr0.straylight.fw.util.QuoteStringFilter;
import org.pr0.straylight.fw.util.HTMLStringFilter;
import org.pr0.straylight.fw.util.SQLEncodeStringFilter;
import org.pr0.straylight.fw.util.TextBlockFormatter;
import org.pr0.straylight.fw.util.StringFilterException;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fw.um.UserManagerException;
import org.pr0.straylight.fw.um.UserNotFoundException;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.db.DBConnectionPool;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fs.context.ContextForum;
import org.pr0.straylight.fs.context.ContextMessage;
import org.pr0.straylight.fs.context.ContextAuthor;
import org.pr0.straylight.fs.context.ContextDate;

/**
 * handles the /forumname/newReply request. It will generates a form for
 * writing a reply to a particular message and than stores this reply in
 * the database.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2002/02/10 18:54:22 $
 */
class NewReplyRequest implements RequestHandler
{
    /**
     * the context object for the response.
     */
    private Hashtable context;

    /**
     * the name of the template which should be used for the reponse.
     */
    private String templatename;

    /**
     * the local logging category.
     */
    private static Category logger = null;
    
    /**
     * string filter for incoming text (incl HTML check)
     */
    private static StringFilterChain incomingFilter = null;

    /**
     * string filter for outgoing text (quoting)
     */
    private static StringFilterChain outgoingFilter = null;
    
    /**
     * handles the request and generates the context for the response.
     *
     * @param request the HTTP request object
     * @param forum the forum object 
     * @param user the User object of the user creating the reply
     * @throws ForumServletException on database access error or database
     *                               content error
     */
    protected NewReplyRequest(HttpServletRequest request, Forum forum,
                              User user)
                              throws ForumServletException
    {
        if(logger == null)
            logger = Category.getInstance(this.getClass().getName());

        // initialize StringFilter if not yet done
        if(incomingFilter == null)
        {
            incomingFilter = new StringFilterChain();
            incomingFilter.add(new TextBlockFormatter(65, "> "));
            incomingFilter.add(new HTMLStringFilter());
            incomingFilter.add(new SQLEncodeStringFilter());
        }
        if(outgoingFilter == null)
        {
            outgoingFilter = 
                new StringFilterChain(new QuoteStringFilter());
        }
        
        String idparam = request.getParameter("message");
        String subject = request.getParameter("subject");
        String body = request.getParameter("body");
        String backurl = request.getParameter("back");
       
        String backenc;
        
        if(backurl != null)
        {
            // if backurl was defined use this for backlink
            backenc = backurl;
            backurl = backurl.replace('$', '&');
        }
        else
        {
            // if not use forumbase
            backurl = request.getContextPath() + request.getServletPath()
                    + "/" + forum.getShortname();
            backenc = backurl.replace('&', '$');
        }
        
        int messageid;
        
        if(idparam == null)
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Invalid request. Paramter 'message' was not "
                        + "defined.");
            throw new ForumServletException(
                "Invalid Request",
                "Your request does not provide needed parameter. "
                + "Request canceled."
            );
        }
        else
        {
            try
            {
                messageid = Integer.parseInt(idparam);
            }
            catch(NumberFormatException e)
            {
                logger.warn("[" + request.getRemoteAddr() + "]: "
                           + "Invalid request. Parameter 'message' has "
                           + "wrong format.");
                throw new ForumServletException(
                    "Invalid Request",
                    "The 'message' parameter you were passing has an "
                    + "invalid format."
                );
            }
        }
        
        // create the context forum
        ContextForum contextforum = new ContextForum(
            forum.getTitle(),
            forum.getDescription(),
            forum.getHomepage(),
            request.getContextPath() + request.getServletPath()
                + "/" + forum.getShortname(),
            null
        );

        if(subject == null && body == null)
        {
            // neither subject or body where submitted, so show first page
            
            // first get the message the reply is for
            String statement = "SELECT ForumMessage.Subject, "
                             + "ForumMessage.Body, "
                             + "ForumMessage.AuthorId, "
                             + "ForumMessage.CreateDate "
                             + "FROM ForumMessage WHERE "
                             + "ForumMessage.ForumId = " + forum.getId()
                             + " AND ForumMessage.Id = " + messageid;
            
            // the ContextMessage object which will contain the quoted
            // message
            ContextMessage contextmessage = null;
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
                        // if the forum is postrestricted than there should
                        // be an author available for this message
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
                    SimpleDate date = new SimpleDate(rs.getString(4));
                    ContextDate contextdate = new ContextDate(
                        date.getDay(),
                        date.getMonth(),
                        date.getYear(),
                        date.getHour(),
                        date.getMin(),
                        date.getSec()
                    );

                    // get subject and body
         
                    String msubject = rs.getString(1);
                    String mbody = rs.getString(2);
                    
                    // add the Re to the subject
                    if(msubject.indexOf("Re: ") != 0)
                        msubject = "Re: " + msubject;
                        
                    // quote the body
                    try
                    {
                        mbody = outgoingFilter.filter(mbody);
                           
                    }
                    catch(StringFilterException e)
                    {
                        logger.warn("[" + request.getRemoteAddr() + "]: "
                                    + "Subject or body of message " 
                                    + messageid + " are incorrect "
                                    + "strings.");
                    }
                    
                    // i need to provide auhtor and date that the template
                    // can generates a 'On $Date $Author wrote: ' line.
                    contextmessage = new ContextMessage(
                        contextauthor,      // the author context
                        msubject,            // the subject
                        mbody,               // the body
                        null,               // the accessurl
                        null,               // the reply url
                        null,               // the reply level
                        false,              // has replies
                        contextdate,        // the date context
                        true,               // is expanded
                        null                // toggle expand url
                    );
                    
                    int error = 0;
                    context = new Hashtable();
                    context.put("Message", contextmessage);
                    context.put("MessageId", new Integer(messageid));
                    context.put("Forum", contextforum);
                    context.put("Error", new Integer(error));
                    context.put("BackLink", backurl);
                    context.put("BackEnc", backenc);
                    templatename = "NewReplyForm";
                    rs.close();
                }
                else
                {
                    rs.close();
                    if(dbcon != null) 
                        ForumServlet.dbpool.releaseConnection(dbcon);
                    
                    logger.warn("[" + request.getRemoteAddr() + "]: "
                                + "Message '" + messageid + " not found.");
                    
                    throw new ForumServletException(
                        "No such message",
                        "The message with message Id '" + messageid + "' "
                        + "you want to reply to could not be found in "
                        + "the current forum."
                    );
                }
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
        }
        else if(body != null && !body.equals("")
                && subject != null && !subject.equals(""))
        {
            boolean htmlcontent = false;
            // both values are defined
            try
            {
                // if there is HTML in the string the following calls will
                // throw an StringFilterException with id
                // "HTMLStringFilter"
                String newbody = incomingFilter.filter(body);
                String newsubject = incomingFilter.filter(subject);

                // the following part is skiped when HTML code was found in
                // one of the strings
                DBConnectionPool dbpool = ForumServlet.dbpool;
                DBConnection dbcon = null;
                
                String statement = "SELECT Thread, ReplyLevel "
                                 + "FROM ForumMessage "
                                 + "WHERE Id = " + messageid
                                 + " AND ForumId = " + forum.getId();
                
                try
                {
                    dbcon = dbpool.getConnection();
                    ResultSet rs = dbcon.executeQuery(statement);
                    if(rs.next())
                    {
                        // the thread is still the same
                        int thread = rs.getInt(1);
                        
                        // the parent message itself is the thread
                        thread = thread == 0 ? messageid : thread;
                        
                        // the new replylevel is one more
                        int replylevel = rs.getInt(2) + 1;
                        
                        if(user != null)
                        {
                            statement = 
                                "INSERT INTO ForumMessage "
                                + "(Subject, Body, ForumId, AuthorId, "
                                + "Thread, ReplyTo, ReplyLevel, "
                                + "HasReply, HostAddress, CreateDate) "
                                + "VALUES ('" + newsubject + "','" 
                                + newbody + "'," + forum.getId() + ","
                                + user.getId() + ", " + thread + ", "
                                + messageid + ", " + replylevel + ","
                                + "0,'" + request.getRemoteAddr() 
                                + "',CURRENT_TIMESTAMP)";
                        }
                        else
                        {
                            statement = 
                                "INSERT INTO ForumMessage "
                                + "(Subject, Body, ForumId, AuthorId, "
                                + "Thread, ReplyTo, ReplyLevel, "
                                + "HasReply, HostAddress, CreateDate) "
                                + "VALUES ('" + newsubject + "','" 
                                + newbody + "'," + forum.getId() + ","
                                + " " + thread + "," + messageid + ","
                                + replylevel + ", 0,"
                                + "0,'" + request.getRemoteAddr() 
                                + "',CURRENT_TIMESTAMP)";
                        }
                        System.out.println("Execute statement: " 
                                           + statement);
                        dbcon.executeQuery(statement);
                        // update reply flag in parent message
                        statement = "UPDATE ForumMessage "
                                  + "SET HasReply = 1 "
                                  + "WHERE Id = " + messageid
                                  + " AND ForumId = " + forum.getId();
                        dbcon.executeQuery(statement);
                    }
                    else
                    {
                        rs.close();
                        if(dbcon != null) 
                            dbpool.releaseConnection(dbcon);
                    
                        logger.warn("[" + request.getRemoteAddr() + "]: "
                                    + "Message '" + messageid 
                                    + " not found.");
                    
                        throw new ForumServletException(
                            "No such message",
                            "The message with message Id '" 
                            + messageid + "' "
                            + "you want to reply to could not be found "
                            + "in the current forum."
                        );
                    }
                    rs.close();
                    dbpool.releaseConnection(dbcon);
                    dbcon = null;
                }
                catch(DBConnectionPoolException e)
                {
                    logger.error("[" + request.getRemoteAddr() + "]: "
                                 + "DBConnectionPool Exception: "
                                 + e.getMessage());
                    throw new ForumServletException(
                        "Database Connection Pool Error",
                        "Cannot get free database "
                            + "connection from database connection "
                            + "pool."
                    );
                }
                catch(SQLException e)
                {
                    if(dbcon != null)
                    {
                        dbpool.releaseConnection(dbcon);
                        dbcon = null;
                    }
                    logger.error("[" + request.getRemoteAddr() + "]: "
                                 + "SQL Exception with statement: '"
                                 + statement + "'. Exception: "
                                 + e.getMessage());
                    throw new ForumServletException(
                        "Message Saving Error",
                        "Cannot save the message in the "
                        + "database. Your request was not "
                        + "processed compeletly, please try "
                        + "again later."
                    );
                }
            } 
            catch(StringFilterException e)
            {
                // if there was html code found in string the exception was
                // thrown with the id HTMLStringFilter...so set the
                // htmlcontent variable to true
                if(e.getFilterName().equals("HTMLStringFilter"))
                {
                    htmlcontent = true;
                }
            }
            // do not need author and date here, because the line should
            // already be in the body.
            ContextMessage contextmessage = 
                new ContextMessage(null, subject, body, null, null, null, 
                                   false, null, false, null);
            
            context = new Hashtable();
            context.put("Forum", contextforum);
            context.put("Message", contextmessage);
            context.put("MessageId", new Integer(messageid));
            context.put("BackLink", backurl);
            context.put("BackEnc", backenc);
            if(htmlcontent)
            {
                // if HTML send also error
                context.put("Error", new Integer(3));
                templatename = "NewReplyForm";
            }
            else
            {
                templatename = "NewReplyCreated";
            }
        }
        else
        {
            // one of the variables was not specified correctly
            int error = 0;
            if(body == null || body.equals(""))
            {
                // there is no body
                error = 1;
                body = "";
            }
            if(subject == null || subject.equals(""))
            {
                // ok there is no subject
                error = 2;
                subject = "";
            }
            ContextMessage contextmessage = 
                new ContextMessage(null, subject, body, null, null, null,
                                   false, null, false, null);
            context = new Hashtable();
            context.put("Forum", contextforum);
            context.put("Message",contextmessage);
            context.put("MessageId", new Integer(messageid));
            context.put("Error", new Integer(error));
            context.put("BackLink", backurl);
            context.put("BackEnc", backenc);
            templatename = "NewReplyForm";
        }
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
