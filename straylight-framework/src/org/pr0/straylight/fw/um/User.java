/* $Id: User.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

package org.pr0.straylight.fw.um;

import org.pr0.straylight.fw.util.SimpleDate;
import org.pr0.straylight.fw.util.UsernameStringFilter;
import org.pr0.straylight.fw.util.EMailStringFilter;
import org.pr0.straylight.fw.util.HomepageStringFilter;
import org.pr0.straylight.fw.util.StringFilterException;

/**
 * The User class implements the default user representation of an User in
 * the Straylight project. A user has some default preferences which can be
 * setted and requested with this class. This class is also the main
 * interface to the UserManager, because the UserManager usually only deals
 * with the User class. This class does not really anything active it is
 * just a structured representation for a user.
 *
 * TODO 
 * 
 * o User Validation
 * o Maybe in later version this objects provides a function to encrypt
 *   the passwords of the users.
 * 
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public class User 
{
    /** 
     * use it for gender male.
     */
    public static int MALE = 0;
    
    /**
     * use it for gender female.
     */
    public static int FEMALE = 1;
   
    /**
     * used for daemons
     */
    public static int DAEMON = 2;
    
    /**
     * the user Id.
     *
     * not defined: -1
     */
    private int id = -1;

    /**
     * the username.
     *
     * not defined: null
     */
    private String username = null;

    /**
     * the password string.
     *
     * TODO encryption ?
     *
     * not defined: null
     */
    private String password = null;

    /**
     * the real surname of the user.
     *
     * not defined: null
     */
    private String surname = null;

    /**
     * the real firstname of the user.
     *
     * not defined: null
     */
    private String firstname = null;
    
    /**
     * the gender.
     *
     * 0   - male    (we are nothing)
     * 1   - female  (they are the Number One)
     * 2   - daemon/not classifyable/not defined as living
     * 
     * not defined: -1
     */
    private int gender = -1;

    /**
     * the eMail address of a user.
     *
     * not defined: null
     */
    private String eMail = null;

    /**
     * the homepage URL.
     *
     * not defined: null
     */
    private String homepage = null;

    /**
     * is the eMail visible to the public.
     *
     * true        - yes
     * false       - no
     */
    private boolean publicEMail = false;

    /**
     * is the homepage address visible to the public.
     *
     * true        - yes
     * false       - no
     */
    private boolean publicHomepage = false;

    /**
     * the timestamp, when this information was changed the last time.
     */
    private SimpleDate lastModified = null;

    /**
     * the timestamp, when this user was created.
     */
    private SimpleDate createDate = null;
    
    /**
     * the public contructor is used to create a new user.
     *
     * @param username the user's username
     * @param password the user's password
     * @param surname the user's surname
     * @param firstname the user's firstname
     * @param gender the user'sm gender
     * @param email the user's eMail address
     * @param homepage the user's homepage URL
     * @param publicEMail true if the user's eMail is public
     * @param publicHomepage true if the user's homepage is public
     */
    public User(String username,
                String password,
                String surname,
                String firstname,
                int gender,
                String email,
                String homepage,
                boolean publicEMail,
                boolean publicHomepage)
           throws UserProfileException
    {
        setUsername(username);
        setPassword(password);
        setSurname(surname);
        setFirstname(firstname);
        setGender(gender);
        setEMail(email);
        setHomepage(homepage);
        setPublicEMail(publicEMail);
        setPublicHomepage(publicHomepage);
    }

    /**
     * the protected constructor which is only available to classes of the
     * same package, is mainly used by the UserManager which reads a valid
     * user profile from a source and creates a new User object.
     *
     * @param id the user's Id
     * @param username the user's username
     * @param password the user's password
     * @param surname the user's surname
     * @param firstname the user's firstname
     * @param gender the user'sm gender
     * @param email the user's eMail address
     * @param homepage the user's homepage URL
     * @param publicEMail true if the user's eMail is public
     * @param publicHomepage true if the user's homepage is public
     * @param lastModified SimpleDate of the last change of the entry
     * @param createDate SimpleDate of the creation of the user
     */
    protected User(int id,
                   String username,
                   String password,
                   String surname,
                   String firstname,
                   int gender,
                   String email,
                   String homepage,
                   boolean publicEMail,
                   boolean publicHomepage,
                   SimpleDate lastModified,
                   SimpleDate createDate)
    {
        this.id = id;
        this.password = password;
        this.username = username;
        this.surname = surname;
        this.firstname = firstname;
        this.gender = gender;
        this.eMail = eMail;
        this.homepage = homepage;
        this.publicEMail = publicEMail;
        this.publicHomepage = publicHomepage;
        this.lastModified = lastModified;
        this.createDate = createDate;
    }
    
    /**
     * this constructor is used by classes extending this class to
     * instantiate its own user with an existing user object.
     *
     * @param user the existing user object
     */
    public User(User user)
    {
        this.id = user.id;
        this.password = user.password;
        this.username = user.username;
        this.surname = user.surname;
        this.firstname = user.firstname;
        this.gender = user.gender;
        this.eMail = user.eMail;
        this.homepage = user.homepage;
        this.publicEMail = user.publicEMail;
        this.publicHomepage = user.publicHomepage;
        this.lastModified = user.lastModified;
        this.createDate = user.createDate;
    }
    
    /**
     * sets the username of the user. 
     *
     * @param username the username to be set
     * @throws UserProfileException if the username contains invalid
     *                              characters
     */
    public void setUsername(String username)
                throws UserProfileException
    {
        try
        {
            username = (new UsernameStringFilter()).filter(username);
        }
        catch(StringFilterException e)
        {
            throw new UserProfileException("Username");
        }
        this.username = username;
    }

    /**
     * sets the password of the users. This function does not encrypt the
     * password, yet, but in future it will probalbly do this.
     *
     * TODO add password encryption
     *
     * @param password the password string
     * @throws UserProfileException if the value of the provided password is
     *                              invalid
     */
    public void setPassword(String password)
                throws UserProfileException
    {
        if(password == null || password.equals(""))
            throw new UserProfileException("Password");
        
        this.password = password;
    }

    /**
     * sets the surname of a user.
     *
     * @param surname the surname to be setted
     * @throws UserProfileException if the value of the provided surname is
     *                              invalid
     */
    public void setSurname(String surname)
                throws UserProfileException
    {
        if(surname == null || surname.equals(""))
            throw new UserProfileException("Surname");
        
        this.surname = surname;
    }

    /**
     * sets the firstname of the user.
     *
     * @param firstname the firstname to be setted
     * @throws UserProfileException if the given firstname is invalid
     */
    public void setFirstname(String firstname)
                throws UserProfileException
    {
        if(firstname == null || firstname.equals(""))
            throw new UserProfileException("Firstname");
        
        this.firstname = firstname;
    }

    /**
     * sets the gender of the user.
     * 
     * @param gender the user's gender
     * @throws UserProfileException if the given gender is not valid
     */
    public void setGender(int gender)
                throws UserProfileException
    {
        if(gender >= MALE && gender <= DAEMON) 
            this.gender = gender;
        else 
            throw new UserProfileException("Gender");
    }
    
    /**
     * sets the eMail address of the user.
     *
     * @param email the user's eMail address
     * @throws UserProfileException if the given eMail value is not valid
     */
    public void setEMail(String eMail)
                throws UserProfileException
    {
        try
        {
            eMail = (new EMailStringFilter()).filter(eMail);
        }
        catch(StringFilterException e)
        {
            throw new UserProfileException("EMail");
        }
        this.eMail = eMail;
    }

    /**
     * sets the homepage URL of the user.
     *
     * @param homepage the user's homepage URL
     * @throws UserProfileException if the given URL is not valid
     */
    public void setHomepage(String homepage)
                throws UserProfileException
    {
        if(homepage != null && !homepage.equals(""))
        {
            try
            {
                homepage = (new HomepageStringFilter()).filter(homepage);
                this.homepage = homepage;
            }
            catch(StringFilterException e)
            {
                throw new UserProfileException("Homepage");
            }
        }
        else
        {
            this.homepage = null;
        }
    }

    /**
     * sets if the user's eMail address is public or not.
     *
     * @param publicEMail true if the user's eMail is public
     */
    public void setPublicEMail(boolean publicEMail)
    {
        this.publicEMail = publicEMail;
    }

    /**
     * sets if the user's homepage URL is public or not.
     *
     * @param publicHomepage true if the user's homepage is public
     */
    public void setPublicHomepage(boolean publicHomepage)
    {
        this.publicHomepage = publicHomepage;
    }

    /**
     * returns the Id of the user.
     *
     * @return the user's id
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * returns the username of the user.
     * 
     * @return the user's username
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * retruns the password of the user.
     * 
     * TODO add password decryption
     *
     * @return the password string
     */
    public String getPassword()
    {
        return this.password;
    }
     
    /**
     * returns the surname of the user.
     *
     * @return the surname
     */
    public String getSurname()
    {
        return this.surname;
    }

    /**
     * returns the firstname of the user.
     *
     * @return the user's firstname
     */
    public String getFirstname()
    {
        return this.firstname;
    }

    /**
     * returns the gender of the user.
     *
     * @return the user's gender
     */
    public int getGender()
    {
        return this.gender;
    }

    /**
     * returns the eMail address of the user.
     *
     * @return the user's eMail address
     */
    public String getEMail()
    {
        return this.eMail;
    }

    /**
     * returns the homepage URL of the user.
     *
     * @return the user's homepage
     */
    public String getHomepage()
    {
        return this.homepage;
    }

    /**
     * returns true if the user was specifying his eMail address as public.
     * 
     * @return true if eMail address is public
     */
    public boolean isPublicEMail()
    {
        return this.publicEMail;
    }

    /**
     * returns true if the user was specifying his homepage as public.
     * 
     * @return true if homepage is public
     */
    public boolean isPublicHomepage()
    {
        return this.publicHomepage;
    }

    /**
     * returns the last modification date as a simple date object.
     *
     * @return the SimpleDate object
     */
    public SimpleDate getLastModified()
    {
        return this.lastModified;
    }

    /**
     * returns the creation date of the user.
     *
     * @return the SimpleDate object
     */
    public SimpleDate getCreateDate()
    {
        return this.createDate;
    }
}
