/* $Id: DBUserManager.java,v 1.2 2001/09/27 16:39:56 racon Exp $ */

package org.pr0.straylight.fw.um;

import java.util.Arrays;
import java.util.Properties;
import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.db.DBConnectionPool;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.util.SimpleDate;

/**
 * This is a UserManager implementation which uses a database backend as
 * the source for user accounts. It works together with the database
 * connection and the database connection pool coming with the Straylight
 * Framework. This UserManager also implements cache functionality. The
 * cache works in the round-robin mode, that means every time a new entry
 * is stored in cache the current entry in cache where the pointer is will
 * be overwritten by the new entry and the pointer will be increased to the
 * next entry. So it can also happens that a frequently used user would be
 * overwritten in the cache and have to be reloaded from the database.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:56 $
 */
public class DBUserManager implements UserManager
{
    /**
     * the default maximum number of users.
     *
     * default: 100
     */
    public static final int MAXUSERS = 100;
    
    /**
     * local logsystem.
     */
    private Category logger = null;
    
    /**
     * the local user cache for Id mapping.
     */
    private Hashtable idCache = null;

    /**
     * the local user cache for username mapping.
     */
    private Hashtable usernameCache = null;
    
    /**
     * table to order the cache content.
     */
    private int[] cacheContent;
    
    /**
     * max number of users in the cache.
     *
     * default: MAXUSERS
     */
    private int maxusers = MAXUSERS;

    /**
     * the cache pointer, points to the position, which should be replaced
     */
    private int cachepos = 0;
    
    /**
     * the local used database connection pool
     */
    private DBConnectionPool dbpool = null;

    /**
     * initializes the instance of the DBUserManager. The DBUserManager
     * gets configured with a list of properties:
     * <dl>
     *  <dt>
     *   <b>DBUserManager.MaxUsers</b>
     *  </dt>
     *  <dd>
     *   defines how big the cache is and the maximum number of users be
     *   cached at one time
     *  </dd>
     * </dl>
     * It will also need a preconfigured and running DBConnectionPool which
     * will be used to get the database connections. If the
     * DBConnectionPool is not defined the constructor tries to get a
     * preconfigured DBConnectionPool instance from the static
     * DBConnectionPool. If this nit work it will throw an Exception. Make
     * sure that no other class, also referencing the same
     * DBConnectionPool, is calling the cleanup() method of the
     * DBConnectionPool as long this user manager should work.
     *
     * @param props the list of properties
     * @param dbpool the preconfigured connection pool
     * @throws UserManagerException on configuration errors
     */
    public DBUserManager(Properties props, DBConnectionPool dbpool)
                         throws UserManagerException
    {
        // initialize logging
        logger = Category.getInstance(this.getClass().getName());
     
        if(props != null)
        {
            try
            {
                // try to get property MaxUsers
                maxusers = Integer.parseInt(
                    props.getProperty(
                        "DBUserManager.MaxUsers",
                        Integer.toString(maxusers)
                    )
                );
                // check if at least 1
                if(maxusers < 1)
                {
                    logger.warn(
                        "Invalid configuration value in property "
                        + "DBUserManager.MaxUsers. MaxUsers must "
                        + "be at least 1. "
                        + "Using default: " + MAXUSERS
                    );
                    maxusers = MAXUSERS;
                }
                logger.info(
                    "Property DBUserManger.MaxUsers was setted to: "
                    + maxusers
                );
            }
            catch(NumberFormatException e)
            {
                // if the value in the property is not allowed
                logger.warn("Invalid configuration value in property "
                            + "DBUserManager.MaxUsers. "
                            + "Set default value: " + MAXUSERS);
                maxusers = MAXUSERS;
            }
        }
        else
        {
            logger.info("No properties defined. Using defaults.");
            maxusers = MAXUSERS;
        }
        if(dbpool != null)
        {
            this.dbpool = dbpool;
        }
        else
        {
            // no connection pool provided
            logger.info("No database connection pool specified, "
                        + "try to get a static preconfigured one.");
            // try to get one from static 
            dbpool = DBConnectionPool.getInstance();
            if(dbpool == null)
            {
                logger.error("No configured database connection "
                             + "pool available. DBUserManager will "
                             + "not work.");
                throw new UserManagerException(
                    "Unable to configure DBUserManager. "
                    + "Cannot get database connection "
                    + "pool."
                );
            }
            else
            {
                logger.info("Got static preconfigured database "
                            + "connection pool.");
            }
        }
        idCache = new Hashtable();
        usernameCache = new Hashtable();
        cacheContent = new int[maxusers];
        // initialize empty cache
        Arrays.fill(cacheContent, -1);
        logger.info("DBUserManager is successfully configured.");
    }
     
    /**
     * returns a user from the database with the specified id. This
     * information can be also served from the internal cache.
     *
     * @param id the id of the requested user
     * @return the user object of the requested user
     * @throws UserNotFoundException if the user was not found in the
     *                               database
     * @throws UserManagerException if there was an error during the 
     *                              database access
     */
    public User getUser(int id) 
                        throws UserNotFoundException, 
                               UserManagerException
    {
        User retval = null;
     
        // try to get user from cache
        retval = (User) idCache.get(new Integer(id));
        if(retval == null)
        {
            // ok user is not in cache
            // try to get user from database
            DBConnection dbcon = null;
            try
            {
                dbcon = dbpool.getConnection();
                ResultSet rs = dbcon.executeQuery(
                    "SELECT Username, Password, "
                    + "Surname, Firstname, Gender, "
                    + "EMail, Homepage, PublicEMail, "
                    + "PublicHomepage, LastModified, "
                    + "CreateDate "
                    + "FROM User "
                    + "WHERE Id=" + id + " LIMIT 1"
                );
                if(rs.next())
                {
                    // user was found in database create user object now
                    retval = new User(
                        id,
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getInt(8) == 1 ? true : false,
                        rs.getInt(9) == 1 ? true : false,
                        new SimpleDate(rs.getString(10)),
                        new SimpleDate(rs.getString(11))
                    );
        
                    // update cache information
                    // lock cache for write operation
                    synchronized(cacheContent)
                    {
                        // before doing anything check again if the user is
                        // still not in the cache (it could happens between
                        // last cache access and now)
                        if(idCache.get(new Integer(id)) == null)
                        {
                            // yes it is still not in cache, so 
                            // delete a user from cache to put the new one
                            // in
                            int olduserid = cacheContent[cachepos];
                            if(olduserid != -1) 
                            {
                                // current cache position is not empty
                                // remove user from idCache
                                User olduser = (User) idCache.remove(
                                    new Integer(olduserid)
                                );
                                // if user was in idCache also remove from
                                // usernameCache
                                if(olduser != null) 
                                    usernameCache.remove(
                                        olduser.getUsername()
                                    );
                            }
                            // set the new user at current position
                            cacheContent[cachepos] = id;
                            // put user in idCache
                            idCache.put(new Integer(id), retval);
                            // put user in usernameCache
                            usernameCache.put(retval.getUsername(), retval);
                            // increase current cache position
                            cachepos++;
                            // but if cache position greater than maxusers
                            // set it back to 0
                            if(cachepos == maxusers) 
                                cachepos = 0;
                        }
                    } // back in not critical part
                }
                else
                {
                    // user was not found in database
                    rs.close();
                    // release the database connection
                    if(dbcon != null) 
                        dbpool.releaseConnection(dbcon); 
                    // throw UserNotFoundException
                    throw new UserNotFoundException(id);
                }
                // close ResultSet
                rs.close();
                // release the database connection
                dbpool.releaseConnection(dbcon);
                dbcon = null;
            }
            catch(SQLException e)
            {
                // release the database connection
                if(dbcon != null) 
                    dbpool.releaseConnection(dbcon); 
                throw new UserManagerException(
                    "Database access failed.",
                    e.getMessage()
                );
            }
            catch(DBConnectionPoolException e)
            {
                throw new UserManagerException(
                    "Cannot get database connection.",
                    e.getMessage()
                );
            }
        }
     
        // return the user
        return retval;
    }
    
    /**
     * returns a user from the database with the specified username. This
     * information can be also served from the internal cache.
     *
     * @param username the username of the requested user
     * @return the user object of the requested user
     * @throws UserNotFoundException if the user was not found in the database
     * @throws UserManagerException if there was an error during the database
     *                              access
     */
    public User getUser(String username) 
                throws UserNotFoundException, UserManagerException
    {
        User retval = null;
     
        // try to get user from cache
        retval = (User) usernameCache.get(username);
        if(retval == null)
        {
            // ok user is not in cache
            // try to get user from database
            DBConnection dbcon = null;
            try
            {
                dbcon = dbpool.getConnection();
                ResultSet rs = dbcon.executeQuery(
                    "SELECT Id, Password, "
                    + "Surname, Firstname, Gender, "
                    + "EMail, Homepage, PublicEMail, "
                    + "PublicHomepage, LastModified, "
                    + "CreateDate "
                    + "FROM User "
                    + "WHERE Username='"
                    + username + "' LIMIT 1"
                );
                if(rs.next())
                {
                    // user was found in database create user object now
                    retval = new User(
                        rs.getInt(1),
                        username,
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getInt(8) == 1 ? true : false,
                        rs.getInt(9) == 1 ? true : false,
                        new SimpleDate(rs.getString(10)),
                        new SimpleDate(rs.getString(11))
                    );
        
                    // update cache information
                    // lock cache for write operation
                    synchronized(cacheContent)
                    {
                        // before doing anything check again if the user is
                        // still not in the cache (it could happens between
                        // last cache access and now)
                        if(usernameCache.get(username) == null)
                        {
                            // yes it is still not in cache, so 
                            // delete a user from cache to put the new one
                            // in
                            int olduserid = cacheContent[cachepos];
                            if(olduserid != -1) 
                            {
                                // current cache position is not empty
                                // remove user from idCache
                                User olduser = (User) idCache.remove(
                                    new Integer(olduserid)
                                );
                                // if user was in idCache also remove from
                                // usernameCache
                                if(olduser != null) 
                                    usernameCache.remove(
                                        olduser.getUsername()
                                    );
                            }
                            // set the new user at current position
                            cacheContent[cachepos] = retval.getId();
                            // put user in idCache
                            idCache.put(
                                new Integer(cacheContent[cachepos]),
                                retval
                            );
                            // put user in usernameCache
                            usernameCache.put(
                                username,
                                retval
                            );
                            // increase current cache position
                            cachepos++;
                            // but if cache position greater than maxusers
                            // set it back to 0
                            if(cachepos == maxusers) 
                                cachepos = 0;
                        }
                    } // back in not critical part
                }
                else
                {
                    // user was not found in database
                    rs.close();
                    // release the database connection
                    if(dbcon != null) 
                        dbpool.releaseConnection(dbcon); 
                    // throw UserNotFoundException
                    throw new UserNotFoundException(username);
                }
                // close ResultSet
                rs.close();
                // release the database connection
                dbpool.releaseConnection(dbcon);
                dbcon = null;
            }
            catch(SQLException e)
            {
                // release the database connection
                if(dbcon != null) 
                    dbpool.releaseConnection(dbcon); 
                throw new UserManagerException(
                    "Database access failed.",
                    e.getMessage()
                );
            }
            catch(DBConnectionPoolException e)
            {
                throw new UserManagerException(
                    "Cannot get database connection.",
                    e.getMessage()
                );
            }
        }
     
        // return the user
        return retval;
    }
    
    /**
     * creates an user in the database with the information from the
     * provided User object. It will not check the information from the
     * user object if they are valid, that is the task of the User class.
     * The source must always follow the implementation of the User class.
     * The method is synchronized so that only one user can be created at
     * one time. It makes sence to lock the whole method, because the
     * createUser() method will be not as much called as a getUser()
     * method.
     *
     * @param user the User object of the new user
     * @return a User object generated from the information from the
     *         database
     * @throws UserExistsException if a user with the provided username
     *                             already exists
     * @throws UserManagerException if there was an error accessing the
     *                              database
     */
    public synchronized User createUser(User user)
                             throws UserExistsException, 
                                    UserManagerException
    {
        // check for user statement
        String custatement = "SELECT Id FROM User WHERE Username='"
                           + user.getUsername() + "' LIMIT 1";
        // create the user
        String crstatement = "INSERT INTO User "
                           + "(Username,Password,Surname,"
                           + "Firstname,Gender,EMail,Homepage,"
                           + "PublicEMail,PublicHomepage,"
                           + "LastModified, CreateDate) VALUES ("
                           + "'" + user.getUsername() + "',"
                           + "'" + user.getPassword() + "',"
                           + "'" + user.getSurname() + "',"
                           + "'" + user.getFirstname() + "',"
                           + user.getGender() + ","
                           + "'"+user.getEMail() + "',"
                           + (user.getHomepage() != null 
                                ? "'" + user.getHomepage() + "'," 
                                : "NULL,")
                           + (user.isPublicEMail() ? "1," : "0,")
                           + (user.isPublicHomepage() ? "1," : "0,")
                           + "CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
        logger.debug("Execute: " + crstatement); 
        DBConnection dbcon = null;
        try
        {   
            dbcon = dbpool.getConnection();
            // check if user already exists
            ResultSet curs = dbcon.executeQuery(custatement);
            if(curs.next())
            {
                // user exists
                // close result set
                curs.close();
                // release connection
                dbpool.releaseConnection(dbcon);
                // throw exception
                throw new UserExistsException(user.getUsername());
            }
            else
            {
                // user does not exists -> create user
                dbcon.executeQuery(crstatement);
            }
            curs.close();
            // release database connection
            dbpool.releaseConnection(dbcon);
            dbcon = null;
        }
        catch(SQLException e)
        {
            if(dbcon != null) 
                dbpool.releaseConnection(dbcon);
            throw new UserManagerException(
                "Cannot create user '"
                + user.getUsername() + "' because of "
                + "database access error.",
                e.getMessage()
            );
        }
        catch(DBConnectionPoolException e)
        {
            throw new UserManagerException(
                "Cannot get free database "
                + "connection from connection "
                + "pool. Cannot create user '"
                + user.getUsername()+"'.",
                e.getMessage()
            );
        }
        // user was successfully created
        logger.info("Created user '" + user.getUsername() + "'.");
        // try to get the new user from the database
        User retval = null;
        try
        {
            retval = getUser(user.getUsername());
        }
        catch(UserNotFoundException e)
        {
            /* should not happens */
        }
     
        // return new user
        return retval;
    }
    
    /** 
     * updates the information about the user in the database according to
     * the given User object. The User object must be a valid user object,
     * with a valid user Id. Except the fixed fields
     * (Id,CreateDate,LastModified) all fields of the user object can be
     * modified. Which fields shouldn't be allowed to change should decide
     * the application using this method. The method is implemented
     * synchronized to prevent a changing of one user two times at the same
     * time.
     *
     * TODO - implement UserNotFoundException throw by checking how many
     *        rows were updated
     * 
     * @param user the valid User object with the new information which
     *             should be updated
     * @return the updated User object how it was retrieved from the
     *         database after updating
     * @throws UserNotFoundException if there was no user found with the
     *                               provided Id
     * @throws UserManagerException if there was an error accessing the
     *                              database
     * @throws UserProfileException if there was user without an Id
     *                              specified
     */
    public synchronized User updateUser(User user)
                             throws UserNotFoundException, 
                                    UserManagerException,
                                    UserProfileException
    {
        // check if it is a valid user object
        if(user.getId() == -1) 
            throw new UserProfileException("Id");
     
        String udstatement = "UPDATE User SET "
                           + "Username='" + user.getUsername() + "',"
                           + "Password='" + user.getPassword() + "',"
                           + "Surname='" + user.getSurname() + "',"
                           + "Firstname='" + user.getFirstname() + "',"
                           + "Gender=" + user.getGender() + "',"
                           + "EMail=" + user.getEMail() + "',"
                           + "Homepage=" 
                                + (user.getHomepage() != null 
                                    ? "'" + user.getHomepage() + "',"
                                    : "NULL,")
                           + "PublicEMail=" 
                                + (user.isPublicEMail() ? "1," : "0,")
                           + "PublicHomepage="
                                + (user.isPublicHomepage() ? "1," : "0,")
                           + "LastModified=CURRENT_TIMESTAMP "
                           + "WHERE Id=" + user.getId();
        
        DBConnection dbcon = null;
        try
        {
            // get database connection
            dbcon=dbpool.getConnection();
            // execute update
            dbcon.executeQuery(udstatement);
        }
        catch(SQLException e)
        {
            dbpool.releaseConnection(dbcon);
            throw new UserManagerException(
                "Cannot update user '"
                + user.getUsername() + "' because of "
                + "database access error.",
                e.getMessage()
            );
        }
        catch(DBConnectionPoolException e)
        {
            throw new UserManagerException(
                "Cannot get free database connection. "
                + "User '" + user.getUsername() + " not "
                + "updated.", 
                e.getMessage()
            );
        }

        // lock the cache because of cache modification
        synchronized(cacheContent)
        {
            // try to get user from cache
            User cacheUser = (User) idCache.get(
                new Integer(user.getId())
            );
      
            if(cacheUser != null)
            {
                // ok user is in cache and probably the old version, 
                // remove old user from cache
                idCache.remove(cacheUser);
                usernameCache.remove(cacheUser);
            } 
        }
     
        // retrieve modified user
        User retval = null;
        try
        {
            retval = getUser(user.getId());
        }
        catch(UserNotFoundException e)
        {
            // should not happens
        }

        return retval;
    } 
    
    /**
     * searches in the source for users matching the user criterias
     * provided in mask and returns an array of User object with a maximum
     * size of limit. This function can search on all criterias on an user
     * incl. the Id and the application using this function have to make
     * sure on which criterias it should not be possible to search.
     *
     * @param mask the UserMask object providing a mask, not defined fiels
     *             means 'no matter'
     * @param limit the maximum number of matches which should be returned
     * @return an array of User objects matching the mask
     * @throws UserManagerException if there were problems accessing the
     *                              database
     */
    public User[] searchUser(User mask, int limit)
                  throws UserManagerException
    {
        return new User[0];
    } 
    
    /**
     * deletes the specified user from the database. It will call the
     * deleteUser(int Id) method to delete the user.
     *
     * @param user the User object of the user which should be deleted
     * @throws UserNotFoundException if the user which should be deleted
     *                               cannot be found in database
     * @throws UserManagerException if there was an error performing
     *                              database operations
     * @throws UserProfileException if there is no Id in User object
     *                              defined
     */
    public void deleteUser(User user)
                throws UserNotFoundException, 
                       UserManagerException,
                       UserProfileException
    {
        deleteUser(user.getId());
    }
    
    /**
     * deletes the user with the specified Id from the database. The method
     * is implemented synchronized to prevent that a user gets deleted
     * 'twice'.
     *
     * @param id the user's id which should be deleted
     * @throws UserNotFoundException if the user which should be deleted
     *                               cannot be found in database
     * @throws UserManagerException if there was an error performing
     *                              database operations
     * @throws UserProfileException if there is an invalid id defined
     */
    public synchronized void deleteUser(int id)
                             throws UserNotFoundException, 
                                    UserManagerException,
                                    UserProfileException
    {
        if(id == -1) 
            throw new UserProfileException("Id");
     
        String custatement = "SELECT Username FROM User WHERE Id=" 
                           + id + " LIMIT 1";
        String delstatement = "UPDATE User SET Deleted=1 WHERE Id=" 
                            + id;
        String username = null;
        DBConnection dbcon = null;
        try
        {
            dbcon = dbpool.getConnection();
            // check if user really exists
            ResultSet curs = dbcon.executeQuery(custatement);
            if(curs.next())
            {
                username = curs.getString(1);
                // yes, user exists
                dbcon.executeQuery(delstatement);
            }
            else
            {
                // no user does not exists
                curs.close();
                dbpool.releaseConnection(dbcon);
                throw new UserNotFoundException(id);
            }
        }
        catch(SQLException e)
        {
            dbpool.releaseConnection(dbcon);
            throw new UserManagerException(
                "Cannot delete user with Id '"
                + id + "' because of "
                + "database access error.",
                e.getMessage()
            );
        }
        catch(DBConnectionPoolException e)
        {
            throw new UserManagerException(
                "Cannot get free database connection. "
                + "User with Id '" + id + " not "
                + "deleted.",
                e.getMessage()
            );
        }
     
        // lock the cache because of cache modification
        synchronized(cacheContent)
        {
            // try to get user from cache
            User cacheUser = (User)idCache.get(new Integer(id));
            if(cacheUser != null)
            {
                // ok user is still in cache ... delete user
                idCache.remove(cacheUser);
                usernameCache.remove(cacheUser);
            } 
        }
        logger.info("User '" + username + "' successfully deleted.");
    }
}
