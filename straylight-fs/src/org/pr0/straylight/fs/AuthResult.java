/* $Id: AuthResult.java,v 1.1 2002/02/05 21:51:41 racon Exp $ */
package org.pr0.straylight.fs;

import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fs.Forum;

/**
 * The AuthResult class represents the result of an authorization request
 * for a particular page served by the forum system.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.1 $ $Date: 2002/02/05 21:51:41 $
 */
class AuthResult
{
    private User user;
    private boolean permited;
    private int type;
    private Forum forum;

    /**
     * creates a new AuthResult object and sets all parameters which can
     * provide the requesting task detailed information about the
     * authentification request.
     *
     * @param user the User object of user wanted to authenticate, if no
     *             user could be found this parameter is <code>null</code>
     * @param permited is set to <code>true</code> if the user was permited
     *                 to access the requested page
     * @param type the type of the auth request (<code>READ</code>,
     *             <code>WRITE</code>)
     * @param forum the forum to which the request was assigned to
     */
    public AuthResult(User user, boolean permited, int type, Forum forum)
    {
        this.user = user;
        this.permited = permited;
        this.type = type;
        this.forum = forum;
    }

    /**
     * retruns the User object of the user which was asking for
     * athorization. If no user was found it will return 
     * <code>null</code>.
     *
     * @return the User object or <code>null</code>
     */
    public User getUser()
    {
        return this.user;
    }

    /**
     * returns <code>true</code> if the request is permited.
     *
     * @return true if request permited
     */
    public boolean isPermited()
    {
        return this.permited;
    }

    /**
     * returns the type of access (<code>READ</code>, <code>WRITE</code>)
     * which was requested.
     *
     * @return the type of request
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * returns the Forum object of the forum the authorization request was
     * for.
     */
    public Forum getForum()
    {
        return this.forum;
    }
}
