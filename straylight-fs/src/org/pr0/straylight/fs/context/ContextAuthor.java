/* $Id: ContextAuthor.java,v 1.2 2001/09/27 16:39:52 racon Exp $ */

package org.pr0.straylight.fs.context;

/**
 * The ContextAuthor is the representative class for an author in the
 * Velocity context.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:52 $
 */
public class ContextAuthor
{
    private String name = "";
    private String firstname = "";
    private String email = "";
    private String fullname = "";
 
    /**
     * The constructor sets the necessary values.
     *
     * @param name the name of the author
     * @param firstname the firstname of the author
     * @param email the eMail address of the author
     */
    public ContextAuthor(String name, String firstname, String email)
    {
        this.name = name;
        this.firstname = firstname;
        this.fullname = firstname + " " + name;
        this.email = email;
    }

    public String getName()
    {
        return name;
    }

    public String getFirstname()
    {
        return firstname;
    }  

    public String getFullname()
    {
        return fullname;
    }

    public String getEMail()
    {
        return email;
    }
}
