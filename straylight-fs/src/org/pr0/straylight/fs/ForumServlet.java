/* $Id: ForumServlet.java,v 1.5 2001/09/27 16:39:50 racon Exp $ */

package org.pr0.straylight.fs;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.servlet.BaseServlet;
import org.pr0.straylight.fw.db.DBConnectionPool;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.tp.TemplateProcessor;
import org.pr0.straylight.fw.tp.TemplateProcessorException;
import org.pr0.straylight.fw.tp.Document;
import org.pr0.straylight.fw.util.Base64;
import org.pr0.straylight.fw.util.StringUtils;
import org.pr0.straylight.fw.um.UserManager;
import org.pr0.straylight.fw.um.DBUserManager;
import org.pr0.straylight.fw.um.UserManagerException;
import org.pr0.straylight.fw.um.UserNotFoundException;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fs.AuthResult;
import java.util.Hashtable;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class provides the main servlet for the Forum System. It
 * is the first instance for preprocessing requests and redirects the main
 * content request to RequestHandler objects which processing the request
 * and returning contexts to create response pages which will be created by
 * this servlet.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.5 $ $Date: 2001/09/27 16:39:50 $
 */
public class ForumServlet extends BaseServlet
{
    // the properties to configure the servlet were provided by the base
    // class BaseServlet.
 
    // XXX just a small page counter fro debug
    private int counter = 0;

    // access methods, also available to other classes of this package
    protected static final int READ = 0;
    protected static final int POST = 1;
 
    // user permissions
    private static final int NOPERMIT = 0;
    private static final int REQUESTED = 1;
    private static final int PERMIT = 2;
 
    // commands
    private static final int UNKNOWN = 0;
    private static final int RECONFIG = 1;
    private static final int SHOWFORUMS = 2;
    private static final int FORGOTPASSWORD = 3;
    private static final int MODIFYUSER = 4;
    private static final int DELETEUSER = 5;
 
    private static final int REGISTERUSER = 51;
    private static final int FORUMADMIN = 52;
 
    private static final int SHOWMESSAGELIST = 101;
    private static final int SHOWMESSAGE = 102;
 
    private static final int NEWTHREAD = 151;
    private static final int NEWREPLY = 152;
 
    // class variables
 
    /**
     * The static database connection pool. It will be initalized by the
     * init of this servlet and after that it is accessable by all classes
     * of this package.
     */
    protected static DBConnectionPool dbpool = null;

    /**
     * The template processor is responsible to create the response pages.
     */
    private TemplateProcessor tp = null;
 
    /**
     * Table which maps the template names to their filenames.
     */
    private Hashtable templates;
 
    /**
     * The cache for the forum objects, so it is not necessary the make a
     * database request for each forum request, which would be on each page
     * request for one forum.
     */
    private Hashtable forumcache;

    /**
     * the Logging system.
     */
    private Category logger;
 
    /**
     * The contentType for all responses.
     */
    private String contentType;
 
    /**
     * the user manager which is responsible to provide and cache general
     * user information like username, password, real name, etc.
     */
    protected static UserManager um = null;
 
    /**
     * The configure method is called on servlet initialization, sets up
     * all the necessary parameter and checks the configuration. It
     * overrides the configure method of the BaseServlet class but it have
     * to make sure to call the original configure method. If an error
     * occures during the configuration process it tries to check the
     * configuration to the end and the BaseServlet class will stop the
     * servlet and produces an error message.
     */
    public void configure()
    {
        // call the original configure method
        super.configure();
  
        // load properties into local variable to save function calls
        Properties props = this.getProperties(); 
  
        // configure logging system
        PropertyConfigurator.configure(props);
     
        // create new logger
        logger = Category.getInstance(this.getClass());
        log("configure: Logging system started. Please see main logfile.");
     
        // first set up the database connection pool
        try
        {
            // initalize the database connection pool with the default
            // configuration properties and add it to the resource table
            dbpool = new DBConnectionPool(props);
            logger.info("configure: Database Connection Pool successfully "
                        + "created.");
        }
        catch(DBConnectionPoolException e)
        {
            foundError();
            logger.error("configure: Cannot create Database "
                         + "Connection Pool: " + e.getMessage());
        }

        // set up the template processor now
        try
        {
            // initalize the template processor with the default properties
            tp = new TemplateProcessor(props);
            logger.info("configure: Template Processor successfully "
                        + "created.");
        }
        catch(TemplateProcessorException e)
        {
            foundError();
            logger.error("configure: Cannot create Template Processor: "
                         + e.getMessage());
        }

        // if template processor available
        if(tp != null)
        {
            // check default document template
            try
            {
                Document doc = new Document(props);
                if(!doc.getTemplate().equals(""))
                {
                    // create a new temporary context
                    Hashtable tmpctx = new Hashtable();
                    // add document to context
                    tmpctx.put("Document", doc);
                    // try to merge context with default document template
                    tp.mergeContext(tmpctx, doc.getTemplate());
                    logger.info("configure: Default document "
                                + "template ok.");
                }
            }
            catch(TemplateProcessorException e)
            {
                foundError();
                logger.error("configure: Cannot merge default "
                             + "document template.");
            }
     
            // check standard templates now
            templates = new Hashtable();
            String[] templateNames = {/*"ShowForums", */"ShowMessageList",
                                      "NewThreadForm", "NewThreadCreated",
                                      "NewReplyForm", "NewReplyCreated",
                                      "ShowMessage",
                                      "AuthorizationFailed",
                                      "RegisterNewUser", 
                                      "RegisterNewUserOk", 
                                      "EnableUserForm",
                                      "EnableUserOk",
                                      "Error"};

            for(int i = 0; i < templateNames.length; i++)
            {
                String templateName = templateNames[i];
                String propsName = "Templates." + templateName;
                try
                {
                    if(props.getProperty(propsName,null) != null)
                    {
                        templates.put(templateName,
                                      props.getProperty(propsName));
                        tp.mergeContext(new Hashtable(),
                                        props.getProperty(propsName));
                        logger.info("configure: '" + templateName
                                    + "' template seems to work "
                                    + "fine.");
                    }
                    else
                    {
                        foundError();
                        logger.error("configure: No '" + templateName 
                                     + "' template defined.");
                    }
                }
                catch(TemplateProcessorException e)
                {
                    foundError();
                    logger.error("configure: '" + templateName
                                 + "' template doesn't work: "
                                 + e.getMessage());
                }
            }
        }
        else
        {
            foundError();
            logger.error("configure: Template Processor not available, "
                         + "skip checking of templates.");
        }
        // setting up UserManager
        try
        { 
            um = new DBUserManager(props,dbpool);
            logger.info("configure: User Manager successfully "
                        + "configured.");
        }
        catch(UserManagerException e)
        {
            foundError();
            logger.error("configure: cannot configure User Manager. "
                         + "Exception: "  +e.getMessage());
        }
        // setting up forum cache
        forumcache = new Hashtable();

        // get content type from configuration properties default is
        // 'text/html'
        contentType = props.getProperty("ContentType","text/html");
    
    }

    /**
     * cleans up the whole configuration of the ForumServlet. It have to make
     * sure that the cleanup method of the BaseServlet is called at the end of
     * this method.
     */
    public void cleanup()
    {
        logger.info("cleanup: Cleaning up...");
     
        // clean up the database connection pool
        if(dbpool != null) 
            dbpool.cleanUp();
     
        // tell the garbage collector to free the memory
        templates = null;
        tp = null;
        forumcache = null;
        logger = null;
     
        super.cleanup();
    }

    /**
     * catches the HTTP GET request from the browser an redirect it to the
     * doRequest method which handles each request by default.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @throws ServletException on an fatal error which cannot be handled by
     *                          the ForumServlet
     * @throws IOException on Input or Output error during write to output
     *                     stream
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
                      throws ServletException, IOException
    {
        doRequest(request, response);
    }
    
    /**
     * catches the HTTP POST request from the browser an redirect it to the
     * doRequest method which handles each request by default.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @throws ServletException on an fatal error which cannot be handled by
     *                          the ForumServlet
     * @throws IOException on Input or Output error during write to output
     *                     stream
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
                       throws ServletException, IOException
    {
        doRequest(request, response);
    }
    
    /**
     * The main request handler. The default handler methods like doGet or
     * doPost will call this method which handles all requests to the
     * ForumServlet.
     *
     * @param request the request object of the current request
     * @param response the response object belonging to the current request
     * @throws ServletException when an fatal error occured which cannot be
     *                          be handled by the ForumServlet
     * @throws IOException on Input or Output error during write to output
     *                     stream
     */
    private void doRequest(HttpServletRequest request,
                           HttpServletResponse response)
                           throws ServletException, IOException
    {
        // XXX debug code
        long millis = System.currentTimeMillis();
        logger.debug("Request: " + request.getPathInfo());
        
        // the short forum name and the name of the action which describes
        // what has do be done
        String shortname = null, actionname = null;
        // the request handler object for the RequestHandler which handles
        // the particular action request
        RequestHandler rh = null;
     
        // the request path as array of strings
        String[] dirs = StringUtils.getPathAsList(request.getPathInfo());
        // try to determine short forum name (if defined) and actionname
        try
        {
            shortname = dirs[0];
            actionname = dirs[1];
            
            // set default action when no action defined
            if(actionname.equals("")) 
                actionname = "showMessageList";
        }
        catch(ArrayIndexOutOfBoundsException e1)
        {
            // ok there is only one parameter defined next try
            shortname = null;
            actionname = null;
            try
            {
                actionname = dirs[0];
                
                // set default action when no action defined
                if(actionname.equals("")) 
                    actionname = "showForums";
            }
            catch(ArrayIndexOutOfBoundsException e2)
            {
                // ok there was nothing specified 
                // default action is showing a list of the forums
                shortname = null;
                actionname = "showForums";
            }
        }
     
        // actionname <=> actionid mapper 
        // performance reasons
        int actionid;
        // most common requests as first
        if(actionname.equals("showMessageList")) 
            actionid = SHOWMESSAGELIST;
        
        else if(actionname.equals("showMessage")) 
            actionid = SHOWMESSAGE;
        
        else if(actionname.equals("showForums")) 
            actionid = SHOWFORUMS;
     
        else if(actionname.equals("newReply")) 
            actionid = NEWREPLY;
     
        else if(actionname.equals("newThread")) 
            actionid = NEWTHREAD;
     
        else if(actionname.equals("registerUser")) 
            actionid = REGISTERUSER;
     
        else if(actionname.equals("modifyUser")) 
            actionid = MODIFYUSER;
     
        else if(actionname.equals("deleteUser")) 
            actionid = DELETEUSER;
     
        else if(actionname.equals("forgotPassword")) 
            actionid = FORGOTPASSWORD;
     
        else if(actionname.equals("reconfig")) 
            actionid = RECONFIG;
     
        else actionid = UNKNOWN;
     
        try
        {
            // application context actions
            if((shortname == null) && (actionid >= 1) && (actionid < 50))
            {
                switch(actionid)
                {
                    case RECONFIG: this.reinit(); break;
                    case SHOWFORUMS: /* do show forums request */ break;
                    case FORGOTPASSWORD: 
                        /* do forgot password request */ break;
                    case MODIFYUSER: /* do modify user request */ break;
                    case DELETEUSER: /* do delete user request */ break;
                }
            }
            // forum context actions
            else if((shortname != null) && (actionid != UNKNOWN))
            {
                // try to get the forum information
                Forum forum = this.getForum(shortname);
       
                // non restricted actions
                if((actionid >= 51) && (actionid < 100))
                {
                    switch(actionid)
                    {
                        case REGISTERUSER:
                            /* do register user request */
                            rh = new RegisterUserRequest(request, forum);
                            break;
                    }
                }
                // process potential read restricted actions
                else if((actionid >= 101) && (actionid < 150))
                {
                    // is this access authorized ?
                    boolean authorizedRead = false;
        
                    // the result of the authentification request
                    AuthResult authres;
                    
                    if(forum.isReadRestricted())
                    {
                        authres = authenticateUser(request, forum, READ);
                        authorizedRead = authres.isPermited();
                    }
                    else
                    {
                        // if forum is not read restricted each request is
                        // authorized
                        authres = new AuthResult(null, true, READ, forum);
                        authorizedRead = true;
                    }
                    if(authorizedRead) 
                    {
                        switch(actionid)
                        {
                            case SHOWMESSAGELIST: 
                                rh = new ShowMessageListRequest(request,
                                                                forum);
                                break;
                            
                            case SHOWMESSAGE: 
                                rh = new ShowMessageRequest(request, 
                                                            forum);
                                break;
                        }
                    }
                    else
                    {
                        // do authorization failed request for reading
                        rh = new AuthorizationFailedRequest(
                            request,
                            response,
                            forum,
                            READ,
                            authres.getUser()
                        );
                    } 
                }
                // process post restricted actions
                else if((actionid >= 151) && (actionid < 200))
                {
                    boolean authorizedPost = false;
        
                    // the result of the authentification request
                    AuthResult authres;
        
                    // ok lets see if that is an post restricted forum 
                    if(forum.isPostRestricted())
                    {
                        authres = authenticateUser(request, forum, POST);
                        authorizedPost = authres.isPermited();
                        //XXX
                        // logger.debug("User: " 
                        //             + authres.getUser().getUsername());
                        }
                    else
                    {
                        // if forum is not post restricted, the request is
                        // authorized in
                        // any case
                        authres = new AuthResult(null, true, POST, forum);
                        authorizedPost = true;
                    }
          
                    if(authorizedPost)
                    {
                        switch(actionid)
                        {
                            case NEWTHREAD: 
                                rh = new NewThreadRequest(
                                    request,
                                    forum,
                                    authres.getUser()
                                ); 
                                break;
                            case NEWREPLY: 
                                rh = new NewReplyRequest(
                                    request,
                                    forum,
                                    authres.getUser()
                                );
                                break;
                        }
                    }
                    else
                    {
                        // do authorization failed for posting access
                        rh = new AuthorizationFailedRequest(
                            request,
                            response,
                            forum,
                            POST,
                            authres.getUser()
                        );
                    }
                }
                // the actionid is not valid for a forum action
                else
                {
                    logger.error("doRequest [" + request.getRemoteAddr()
                                 + "]: Unknown command requested.");
                    
                    throw new ForumServletException(
                        "Unknown Command",
                        "The command sent with your request "
                        + "is unknown. The request cannot be "
                        + "processed."
                    );
                } 
            }
            else
            {
                logger.error("doRequest [" + request.getRemoteAddr()
                             + "]: Unknown command requested.");
       
                throw new ForumServletException(
                    "Unknown Command",
                    "The command sent with your request "
                    + "is unknown. The request cannot be "
                    + "processed."
                );
            }
        }
        catch(ForumServletException e)
        {
            rh = new ErrorPageRequest(e.getTitle(), e.getMessage());
        }
        // set content type
        response.setContentType(contentType);
     
        String result = null;
        try
        {
            // create the output document
            if(rh != null)
            {
                // add some addition debug info XXX
                logger.debug("Context is " + rh.getClass().getName());
                rh.getContext().put("Counter", "Counter: " + (counter++)
                                    + " Time (ms): "
                                    + (System.currentTimeMillis() 
                                        - millis));
                result = tp.createDocument(
                    rh.getContext(),
                    (String)templates.get(rh.getTemplateName())
                );

                // add some addition debug info XXX
                result += "\n<!-- Document generated by Straylight in "
                       + (System.currentTimeMillis() - millis) 
                       + " ms. -->\n";
            }
        }
        catch(TemplateProcessorException e)
        {
            logger.error("doRequest [" + request.getRemoteAddr() + "]: "
                         + "TemplateProcessor Exception: "
                         + e.getMessage());
            throw new ServletException("Template Processor Error: Cannot "
                                       + "process template: "
                                       + rh.getTemplateName());
        }
        response.getWriter().print(result);
    } 
    
    /**
     * returns a Forum object from cache if there is already one existing
     * with the same shortname, if not, the requested Forum object will be
     * created with the information from the database. The method uses a
     * database connection if it creates a new Forum object.
     *
     * @param shortname the shortname of the forum
     * @throws ForumServletException when an error occures during the
     *                               creating of a new Forum object
     */
    private Forum getForum(String shortname) 
                           throws ForumServletException
    {
        Forum forum = null;
        DBConnection dbcon = null; // <-- this is needed here because if 
                                   //     I do that
                                   //     in the try block I cannot
                                   //     reference the connection in the
                                   //     catch(ForumException) block and
                                   //     cannot release the connection if
                                   //     there was an error during the
                                   //     creation of the new Forum object.
                                   //     So the connection will be in use
                                   //     until the DBPool observer destroy
                                   //     it because of a timeout. Can be a
                                   //     long time.
     
        // if no forum in cache create one and put it into the cache
        if((forum = (Forum)forumcache.get(shortname)) == null)
        {
            try
            {
                // get a database connection
                dbcon = dbpool.getConnection();
                // create the Forum object
                forum = new Forum(dbcon,shortname);
                // release the database connection
                dbpool.releaseConnection(dbcon);
                // put the Forum object to the cache
                forumcache.put(shortname,forum);
            }
            // there were problems getting the database connection
            catch(DBConnectionPoolException e)
            {
                log("getForum [n/a]: "
                    + "DBConnectionPool Exception: " + e.getMessage());
                throw new ForumServletException(
                    "Database Connection Pool Error",
                    "Cannot get free database connection "
                    + "from database connection pool."
                );
            }
            // there were problems creating the Forum object
            catch(ForumException e)
            {
                // free the used connection here
                if(dbcon != null)
                {
                    dbpool.releaseConnection(dbcon);
                }
                log("getForum [n/a]: "
                    + "Forum Exception: " + e.getMessage());
                throw new ForumServletException(
                    "Opening Forum Error",
                    "An error occured during getting "
                    + "information about the requested forum."
                );
            }
        }
        return forum;
    }

    /**
     * checks if a user wants to authorized and if the user has permissions
     * to do that for the requested type of request. The functions returns
     * a User object if an User authorized sucessfully and null if there
     * was no user want to authorized or the authorization of the user
     * failed.
     *
     * @param request the HTTP request object to extract HTTP header
     *                information
     * @param forum the forum object with some informations about the forum
     * @param type the type of authorization the user has to have
     *             permittions for
     */
    private AuthResult authenticateUser(HttpServletRequest request,
                                        Forum forum, 
                                        int type) 
                                        throws ForumServletException
    {
        User user = null;
        boolean permited = false;
        DBConnection dbcon = null; // <-- this is needed here because if I
                                   //     do that in the try block I cannot
                                   //     reference the connection later
                                   //     when I want release the
                                   //     connection So the connection will
                                   //     be in use until the DBPool
                                   //     observer destroy it because of a
                                   //     timeout. Can be a long time.
     
        String encodedAuthReq = request.getHeader("Authorization");
        if(encodedAuthReq != null)
        {
            // ok there is somebody who wants to authorize
            // remove "BASIC "
            String encodedPassword = encodedAuthReq.substring(6);
      
            // decode the Authentification field, contains username and
            // password separated by ':'
            String unpw = Base64.decode(encodedPassword);
            String username = null;
            String password = null;
            if(unpw != null)
            {
                // split the string
                username = unpw.substring(0, unpw.indexOf(":"));
                password = unpw.substring(unpw.indexOf(":") + 1);
                // ok lets check user

                try
                {
                    // try to get the user object
                    user = um.getUser(username);
                }
                catch(UserNotFoundException e)
                {
                    // we don't have a user
                    user = null;
                }
                catch(UserManagerException e)
                {
                    logger.error("authenticateUser [" 
                                 + request.getRemoteAddr() + "]: "
                                 + "Cannot authenticate user. "
                                 + "Exception: " + e.getMessage());
                    throw new ForumServletException(
                        "User Manager Error",
                        "Cannot authenticate user because "
                        + "of an error in the User Manager "
                        + "service."
                    );
                }
                // ok I should have either no user or a users with a
                // password and an ID if I have no user than she/he fails
                if(user != null)
                {
                    // ok check whether her/his password is correct
                    if(user.getPassword().equals(password))
                    {
                        // ok she/he provided the right password, lets see
                        // if she/he has the requested permissions on the
                        // forum
                        String statement = "";
                        if(type == READ)
                        {
                            // get the read flag
                            statement = "SELECT DISTINCT ReadPermit "
                                      + "FROM ForumUserMap "
                                      + "WHERE ForumId=" + forum.getId() 
                                      + " AND UserId=" + user.getId();
                        }
                        else if(type == POST)
                        {
                            // get the post flag
                            statement = "SELECT DISTINCT PostPermit "
                                      + "FROM ForumUserMap "
                                      + "WHERE ForumId=" + forum.getId()
                                      + " AND UserId=" + user.getId();
                        }
                        try
                        {
                            // I need a database connection to do this
                            dbcon = dbpool.getConnection();
                            ResultSet rs = dbcon.executeQuery(statement);
                            if(rs.next())
                            {
                                // if ResultSet defined 
                                if(rs.getInt(1) == PERMIT)
                                { 
                                    // the user has access to the requested
                                    // area
                                    permited = true;
                                }
                            }
                        }
                        catch(SQLException e)
                        {
                            if(dbcon != null)
                            {
                                // free the used connection here
                                dbpool.releaseConnection(dbcon);
                                dbcon = null;
                            }
                            logger.error("authenticateUser ["
                                         + request.getRemoteAddr() + "]: "
                                         + "SQL Exception with "
                                         + "statement: '" + statement
                                         + "' Exception: "
                                         + e.getMessage());
                            throw new ForumServletException(
                                "Database Error",
                                "Cannot retrieve user permissions "
                                + "from database."
                            );
                        }
                        catch(DBConnectionPoolException e)
                        {
                            logger.error("authenticateUser [" 
                                         + request.getRemoteAddr() + "]: "
                                         + "DBConnectionPool Exception: "
                                         + e.getMessage());
                            throw new ForumServletException(
                                "Database Connection Pool Error",
                                "Cannot get free database "
                                + "connection from database "
                                + "connection pool."
                            );
                        }
                    }
                    else
                    {
                        // ok she/he provided a wrong password
                        // log error message for security reasons
                        logger.warn("authenticateUser [" 
                                    + request.getRemoteAddr() + "]: "
                                    + "Authorization for user '"
                                    + user.getUsername() + "' failed.");
                    }
                } // end of if(user != null)
            } // end of if(unpw != null) ... 
            // user is initalized with null so I do not
            // need an else here
      
            // realase a used database connection
            if(dbcon != null)
            {      
                dbpool.releaseConnection(dbcon);
                dbcon = null;
            }
        } // end of if(encodedAuthReq!=null) ok nobody wants to authorize
     
        return new AuthResult(user, permited, type, forum);
    }
}
