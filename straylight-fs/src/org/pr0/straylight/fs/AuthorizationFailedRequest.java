/* $Id: AuthorizationFailedRequest.java,v 1.2 2001/08/26 02:09:12 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fs.context.ContextForum;

/**
 * Handles the AuthorizationFailedRequest and prepares a context for an
 * AuthorizationFailedPage which provides additional information to the
 * user made the request. The Handler will also send the special error code
 * (401) to the browser.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/08/26 02:09:12 $
 */
class AuthorizationFailedRequest implements RequestHandler
{
    private Hashtable context;
    private String templatename;
    
    /**
     * Prepares the context.
     * 
     * @param request the request object
     * @param response the response object
     * @param forum the forum which the user wanted to access
     * @param type the type of failed authorization read/write
     * @param user the user of which the authorization failed or
     *             <code>null</code>
     */
    protected AuthorizationFailedRequest(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Forum forum,
                                         int type,
                                         User user)
    {
        String retval;
     
        // creating new context
        context = new Hashtable();
        // creating new context forum for the context
     
        ContextForum forumctx = new ContextForum(
            forum.getTitle(),
            forum.getDescription(),
            forum.getHomepage(),
            request.getContextPath() + request.getServletPath() + "/"
                                     + forum.getShortname(),
            null
        );
        
        // add the forum context to the context
        context.put("Forum", forumctx);
        // add the base url of the Forum System to the context
        context.put("Base", request.getContextPath() 
                            + request.getServletPath());
        
        // if this is a read request
        if(type == ForumServlet.READ)
        {
            // tell the template which request it was
            context.put("RestrictionType", new Integer(0));
            // tell the template if it should create a link to the
            // registration page
            context.put(
                "RegistrationAllowed", 
                new Integer(forum.isReadRegistrationAllowed() ? 1 : 0)
            );
            
            // send the right header to browser
            response.setHeader(
                "WWW-Authenticate",
                "Basic realm=\"Read access to area: "
                    + forum.getShortname() + "\""
            );
        }
        // if this is a post request and the registration allowd
        else if(type == ForumServlet.POST)
        {
            // tell the template which request it was
            context.put("RestrictionType", new Integer(1));
            // tell the template if it should create a link to the
            // registration page
            context.put(
                "RegistrationAllowed", 
                 new Integer(forum.isPostRegistrationAllowed() ? 1 : 0)
            );
            // send the right header to browser
            response.setHeader(
                "WWW-Authenticate",
                "Basic realm=\"Post access to area: "
                + forum.getShortname() + "\""
            );
        }
        // if the user is know which had no access to a resource then
        // create a link to the enable page
        if(user != null)
        {
            context.put("UserID", new Integer(user.getId()));
        }
        /*
         * XXX XXX XXX XXX
         *
         * implement back link scheme
         *
        String requestedURL = request.getContextPath() 
                            + request.getServletPath();
        if(request.getQueryString() != null)
            requestedURL += request.getQueryString();
            
        context.put("BackLink", requestedURL.replace('&', '$'));
         */
        // set the error code of the response
        // even if this method is not suggested I will do that, I have no
        // other choice to provide my own page
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        templatename = "AuthorizationFailed";
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
