/* $Id: NewThreadRequest.java,v 1.2 2001/08/26 02:09:13 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.db.DBConnectionPool;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.util.StringFilterChain;
import org.pr0.straylight.fw.util.HTMLStringFilter;
import org.pr0.straylight.fw.util.SQLEncodeStringFilter;
import org.pr0.straylight.fw.util.TextBlockFormatter;
import org.pr0.straylight.fw.util.StringFilterException;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fs.context.ContextForum;
import org.pr0.straylight.fs.context.ContextMessage;

/**
 * Handles the /forumname/newThread request to create a new thread in the
 * forum.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:13 $
 */
class NewThreadRequest implements RequestHandler
{
    /**
     * pick up the context for the page.
     */
    private Hashtable context;
    
    /**
     * picks up the template name for the reponse page.
     */
    private String templatename;
 
    /**
     * the local logger.
     */
    private static Category logger = null;
    
    /**
     * the string filter for incoming text.
     */
    private static StringFilterChain incomingFilter = null;
    
    /**
     * processes the request and generates the context for the response page.
     * It expects a Forum and a User object. If the User object is
     * 'null' the request is handles anonymous and the author of the message
     * will not be setted, otherwise the userid of the user will be set as the
     * authorid.
     *
     * @param request the HTTP request object
     * @param forum the forum object, cannot be 'null'
     * @param user the User object, can be 'null'
     * @throws ForumServletException on database access error
     */
    protected NewThreadRequest(HttpServletRequest request, Forum forum, 
                               User user) 
                               throws ForumServletException 
    {
        if(logger == null) 
            logger = Category.getInstance(this.getClass().getName());
        
        if(incomingFilter == null)
        {
            incomingFilter = new StringFilterChain();
            incomingFilter.add(new TextBlockFormatter(65, "> "));
            incomingFilter.add(new HTMLStringFilter());
            incomingFilter.add(new SQLEncodeStringFilter());
        }
            
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
       
        // creating context with forum information
        ContextForum contextforum = new ContextForum(
            forum.getTitle(),
            forum.getDescription(),
            forum.getHomepage(),
            request.getContextPath() + request.getServletPath()
                + "/" + forum.getShortname(),
            null
        );

        if(body == null && subject == null)
        {
            // ok there was no try to post anything
            ContextMessage contextmessage = 
                new ContextMessage(null, "", "", null, null, null, false,
                                   null, false, null);
            int error = 0;
            context = new Hashtable();
            context.put("Forum", contextforum);
            context.put("Message", contextmessage);
            context.put("Error", new Integer(error));
            context.put("BackLink", backurl);
            context.put("BackEnc", backenc);
            templatename = "NewThreadForm";
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
                String statement;
                if(user != null)
                {
                    statement = "INSERT INTO ForumMessage "
                              + "(Subject, Body, ForumId, AuthorId, "
                              + "Thread, ReplyTo, ReplyLevel, "
                              + "HasReply, HostAddress, CreateDate) "
                              + "VALUES ('" + newsubject + "','" 
                              + newbody + "'," + forum.getId() + ","
                              + user.getId() + ", NULL, NULL, 0,"
                              + "0,'" + request.getRemoteAddr() 
                              + "',CURRENT_TIMESTAMP)";
                }
                else
                {
                    statement = "INSERT INTO ForumMessage "
                              + "(Subject, Body, ForumId, AuthorId, "
                              + "Thread, ReplyTo, ReplyLevel, "
                              + "HasReply, HostAddress, CreateDate) "
                              + "VALUES ('" + newsubject + "','" 
                              + newbody + "'," + forum.getId() + ","
                              + " NULL, NULL, NULL, 0,"
                              + "0,'" + request.getRemoteAddr() 
                              + "',CURRENT_TIMESTAMP)";
                }
                try
                {
                    dbcon = dbpool.getConnection();
                    dbcon.executeQuery(statement);
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
            ContextMessage contextmessage = 
                new ContextMessage(null, subject, body, null, null, null, 
                                   false, null, false, null);
            context = new Hashtable();
            context.put("Forum", contextforum);
            context.put("Message", contextmessage);
            context.put("BackLink", backurl);
            context.put("BackEnc", backenc);
            if(htmlcontent)
            {
                // if HTML send also error
                context.put("Error", new Integer(3));
                templatename = "NewThreadForm";
            }
            else
            {
                templatename = "NewThreadCreated";
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
            context.put("Error", new Integer(error));
            context.put("BackLink", backurl);
            context.put("BackEnc", backenc);
            templatename="NewThreadForm";
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
