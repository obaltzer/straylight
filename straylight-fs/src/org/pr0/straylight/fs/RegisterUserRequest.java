/* $Id: RegisterUserRequest.java,v 1.1 2002/02/05 21:51:41 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fw.um.UserProfileException;
import org.pr0.straylight.fw.um.UserNotFoundException;
import org.pr0.straylight.fw.um.UserExistsException;
import org.pr0.straylight.fw.um.UserManagerException;
import org.pr0.straylight.fs.Forum;
import org.pr0.straylight.fs.context.ContextForum;
import org.pr0.straylight.fw.db.DBConnectionPool;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The RegisterUserRequest Class is responsible to register a new user in
 * the Forum System. It handles the request action command
 * 'registerUser'.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2002/02/05 21:51:41 $
 */
class RegisterUserRequest implements RequestHandler
{
    /**
     * the context for the response page.
     */
    private Hashtable context;

    /**
     * the template name for the response page.
     */
    private String templatename;
    
    /**
     * the logger instance for this class.
     */
    private static Category logger = null;
    
    /**
     * handles the 'registerUser' request.
     *
     * @param request the HttpServletRequest object for this request
     * @param forum the Forum object
     */
    protected RegisterUserRequest(HttpServletRequest request,
                                  Forum forum)
                                  throws ForumServletException
    {
        if(logger == null)
            logger = Category.getInstance(this.getClass().getName());

        String type = request.getParameter("type");
        
        // check the type parameter for correctness
        if(type == null || !(type.equals("post") || type.equals("read")))
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Request with unknown type.");
            throw new ForumServletException(
                "Unknown type",
                "The request was made with an unknown type. "
                + "Cannot proceed."
            );
        }
        
        // check if forum allows registration for this type
        if(type.equals("post") && !forum.isPostRegistrationAllowed())
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Post registration wanted, but not allowed.");
            throw new ForumServletException(
                "Registration forbidden.",
                "The 'POST' registration for this forum is forbidden."
            );
        }
        if(type.equals("read") && !forum.isReadRegistrationAllowed())
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Read registration wanted, but not allowed.");
            throw new ForumServletException(
                "Registration forbidden.",
                "The 'READ' registration for this forum is forbidden."
            );
        }
        
        String enableval = request.getParameter("enable");
        String uidval = request.getParameter("uid");
        String passwd = request.getParameter("password");
        
        // check if I just have to enable a user
        boolean enable = false;
        if(enableval != null && enableval.length() == 1 
            && enableval.charAt(0) == '1')
        {
            enable = true;
        }
        
        int uid = -1;
        if(uidval != null)
        {
            try
            {
                uid = Integer.parseInt(uidval);
            }
            catch(NumberFormatException e)
            {
                logger.error("[" + request.getRemoteAddr() + "]: "
                             + "Invalid UID format. Wrong parameter.");
                throw new ForumServletException(
                    "Wrong Parameter",
                    "The given UID is invalid."
                );
            }
        }
        
        if(uid > 0)
        {   
            User user = null;
            try
            {
                user = ForumServlet.um.getUser(uid);
            }
            catch(UserNotFoundException e)
            {
                // user with given ID couldn't be found
                logger.warn("RegisterUserRequest: "
                            + "Cannot enable user with ID " + uid
                            + " no such user.");
                throw new ForumServletException(
                    "No such user",
                    "Cannot enable user with given ID. No such user."
                );
                
            }
            catch(UserManagerException e)
            {
                // UserManager Error
                logger.error("RegisterUserRequest: "
                             + "User Manager Exception: "
                             + e.getMessage());
                throw new ForumServletException(
                    "User Manager Error",
                    "Cannot get user. There was an error with the "
                    + "User Manager System."
                );
            }
            if(user != null)
            {
                if(passwd == null || passwd.equals(""))
                {
                    context = createBaseContext(request, forum, type, null);
                    context.put("Username", user.getUsername());
                    context.put("UserId", new Integer(user.getId()));
                    templatename = "EnableUserForm";
                }
                else
                {
                    context = createBaseContext(request, forum, type,
                                                null);
                    context.put("Username", user.getUsername());
                    context.put("UserId", new Integer(user.getId()));
                    logger.debug("Password is: '" + user.getPassword() +
                                 "'");
                    logger.debug("Password is: '" + passwd +
                                 "'");
                    if(passwd.equals(user.getPassword()))
                    {
                        // the following metrhod will throw an
                        // ForumServletException, so that the followed code
                        // will not be reached on errors
                        enableUserForForum(forum, user, type); 
                        templatename = "EnableUserOk";
                    }
                    else
                    {
                        logger.warn("RegisterUserRequest: "
                                    + "Authorization failed for user '" 
                                    + user.getUsername() + "' while "
                                    + "trying to enable the user.");
                        context.put("error", "Password");
                        templatename = "EnableUserForm";
                    }
                }
            }
        }
        else
        {
            // context with error indicator
            Hashtable econtext = new Hashtable();
            
            // XXX no user id given so create a new user
            String username = request.getParameter("username");
            String surname = request.getParameter("surname");
            String firstname = request.getParameter("firstname");
            String genderval = request.getParameter("gender");
            String email = request.getParameter("email");
            String homepage = request.getParameter("homepage");
            boolean publicEMail = 
                request.getParameter("publicemail") != null 
                    &&  request.getParameter("publicemail").equals("y") 
                    ? true : false;
                    
            boolean publicHomepage = 
                request.getParameter("publicehomepage") != null 
                    &&  request.getParameter("publichomepage").equals("y") 
                    ? true : false;
            
            // get gender
            int gender = -1;
            if(genderval != null)
            {
                if(genderval.equals("m")) 
                    gender = User.MALE;
                else if(genderval.equals("f"))
                    gender = User.FEMALE;
            }
            
            User newUser = null;
            
            if(username != null)
            {
                // username was defined...
                // try to create a new user
                try
                {   
                    newUser = new User(username, passwd, surname, 
                                       firstname, gender, email, 
                                       homepage, publicEMail,
                                       publicHomepage);
                }
                catch(UserProfileException e)
                {
                    econtext.put("error", e.getField());
                    logger.debug("Create User: error in field '" + e.getField()
                                 + "'");
                }
            } // if the username was not defined don't try to create one
            
           
            // if one of the fields is not correct, show the 
            // registration form again
            if(newUser == null)
            { 
                // show the form with all know variables included.
                this.context = createFormContext(request, forum, type,
                                                 econtext);
                templatename = "RegisterNewUser";
            }
            else
            {
                User createdUser = null;
                // insert user into database
                try
                {
                    createdUser = ForumServlet.um.createUser(newUser);
                }
                catch(UserExistsException e)
                {
                    // user already exists
                    econtext.put("error", "UsernameExists");
                    logger.debug("User '" + newUser.getUsername() 
                                 + "' allready exists.");
                    // print out the registration form again 
                    this.context = createFormContext(request, forum, type,
                                                     econtext);
                    templatename = "RegisterNewUser";
                }
                catch(UserManagerException e)
                {
                    // maybe a database or table error
                    logger.error("Error creating a new user: " 
                                 + e.getMessage());
                    throw new ForumServletException(
                        "Cannot create user!",
                        "Cannot create user '" + newUser.getUsername() 
                            + "'. Try again later."
                    );
                }
                catch(UserProfileException e)
                {
                    // this shouldn't happen because the profile check is
                    // done earlier when the User object is created
                    econtext.put("error", e.getField());
                    logger.debug("Create User: error in field '" 
                                 + e.getField());
                    // print out registration form
                    this.context = createFormContext(request, forum, type,
                                                     econtext);
                    templatename = "RegisterNewUser";
                }
                    
                if(createdUser != null)
                {
                    logger.debug("User '" + createdUser.getUsername()
                                 + "' has been successfully created "
                                 + "with UID " + createdUser.getId()
                                 + ".");
                    
                    enableUserForForum(forum, createdUser, type); 
                    
                    context = createBaseContext(request, forum, type,
                                                null);
                    // back link
                    /* XXX implement back link
                    String backurl = request.getParameter("back");
                    if(backurl != null && !backurl.equals(""))
                        backurl = backurl.replace('$', '&');
                    context.put("BackLink", backurl);
                     */
                    // username
                    context.put("Username", createdUser.getUsername());
                    templatename = "RegisterNewUserOk";
                }
            }
        }
    }
    
    private void enableUserForForum(Forum forum, User user, String type)
                 throws ForumServletException
    {
        // enable the user for the forum if type is defined and
        // registration allowed
        String checkstatement = 
            "SELECT DISTINCT UserId FROM "
            + "ForumUserMap WHERE "
            + "ForumId = " + forum.getId() + " AND "
            + "UserId = "  + user.getId();

        DBConnection dbcon = null;
        try
        {
            dbcon = ForumServlet.dbpool.getConnection();
            ResultSet rs = dbcon.executeQuery(checkstatement);
            if(rs.next())
            {
                // only update
                String stm = "UPDATE ForumUserMap SET ";
                
                if(type.equals("post"))
                    stm += "PostPermit = 2 ";
                else if(type.equals("read"))
                    stm += "ReadPermit = 2 ";
                
                stm += "WHERE UserId = " + user.getId()
                     + " AND "
                     + "ForumId = " + forum.getId();
                // execute the update
                dbcon.executeQuery(stm);
            }
            else
            {
                // create the forum - user - map
                String stm = "INSERT INTO  ForumUserMap "
                           + "VALUES ( " 
                           + forum.getId() + ", "
                           + user.getId() + ", "
                           + "0, ";
                if(type.equals("post"))
                    stm += "0, 2";
                else if(type.equals("read"))
                    stm += "2, 0";
                stm += ")";
                // execute insertion 
                dbcon.executeQuery(stm);
            }
        }
        catch(SQLException e)
        {
            if(dbcon != null)
            {
                ForumServlet.dbpool.releaseConnection(dbcon);
                dbcon = null;
            }
        
            logger.error("RegisterUserRequest: "
                         + "Cannot enable user in "
                         + "ForumUserMap table. SQLException: "
                         + e.getMessage());
            throw new ForumServletException(
                "Database Error",
                "Cannot enable user in ForumUserMap table."
            );
        }
        catch(DBConnectionPoolException e)
        {
            logger.error("RegisterUserRequest: "
                         + "DBConnectionPoolException: "
                         + e.getMessage());
            throw new ForumServletException(
                "Database Connection Pool Error",
                "Cannot get free database connection "
                + "from connection pool."
            );
        }
    }
        
    private Hashtable createBaseContext(HttpServletRequest request,
                                        Forum forum, String type,
                                        Hashtable econtext)
    {
        Hashtable c = new Hashtable();
        
        c.put("Type", type);
        
        String baseurl = request.getContextPath()
                       + request.getServletPath();
        c.put("BaseURL", baseurl);                
        c.put("Forum", 
            new ContextForum(
                forum.getTitle(),
                forum.getDescription(),
                forum.getHomepage(),
                baseurl + "/" + forum.getShortname(),
                null)
        );
        
        // add the error context if it is defined
        if(econtext != null)
            c.putAll(econtext);

        return c;
    }
        
    private Hashtable createFormContext(HttpServletRequest request,
                                        Forum forum, String type)
    {
        return createFormContext(request, forum, type, null);
    }
    
    private Hashtable createFormContext(HttpServletRequest request, 
                                        Forum forum, String type,
                                        Hashtable econtext)
    {
        Hashtable c = createBaseContext(request, forum, type, econtext);
        
        String tmp;
        if((tmp = request.getParameter("username")) != null)
            c.put("Username", tmp);
        if((tmp = request.getParameter("password")) != null)
            c.put("Password", tmp);
        if((tmp = request.getParameter("surname")) != null)
            c.put("Surname", tmp);
        if((tmp = request.getParameter("firstname")) != null)
            c.put("Firstname", tmp);
        if((tmp = request.getParameter("email")) != null)
            c.put("EMail", tmp);
        if((tmp = request.getParameter("gender")) != null)
            c.put("Gender", tmp);
        if((tmp = request.getParameter("homepage")) != null)
            c.put("Homepage", tmp);
        if((tmp = request.getParameter("publicemail")) != null)
            c.put("publicEMail", tmp);
        if((tmp = request.getParameter("publichomepage")) != null)
            c.put("publicHomepage", tmp);

        return c;
    }

    public Hashtable getContext()
    {
        return this.context;
    }

    public String getTemplateName()
    {
        return this.templatename;
    }
}
