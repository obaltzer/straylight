/* $Id: UserManager.java,v 1.2 2001/09/27 16:39:57 racon Exp $ */

package org.pr0.straylight.fw.um;

/**
 * The UserManager is an inteface which is used by Straylight modules to
 * authenticade users and get information about users from a source. The
 * type of the source depends on the implementation of this interface. The
 * UserManager implementation should also provide a caching of user
 * informations to reduce accesses to the source (e.g. database) but should
 * always keeps the data consistent.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:57 $
 */
public interface UserManager
{
    /**
     * returns a user from the source with the specified id. This
     * information can be also served from the internal cache.
     *
     * @param id the id of the requested user
     * @return the user object of the requested user
     * @throws UserNotFoundException if the user was not found in the
     *                               source
     * @throws UserManagerException if there was an error during the source
     *                              access
     */
    public User getUser(int id) 
                throws UserNotFoundException, UserManagerException;

    /**
     * returns a user from the source with the specified username. This
     * information can be also served from the internal cache.
     *
     * @param username the username of the requested user
     * @return the user object of the requested user
     * @throws UserNotFoundException if the user was not found in the
     *                               source
     * @throws UserManagerException if there was an error during the source
     *                              access
     */
    public User getUser(String username) 
                throws UserNotFoundException, UserManagerException;
    
    /**
     * creates an user in the source with the information from the provided
     * User object. It will not check the information from the user object
     * if they are valid, that is the task of the User class. The source
     * must always follow the implementation of the User class.
     *
     * @param user the User object of the new user
     * @return a User object generated from the information from the source
     * @throws UserExistsException if a user with the provided username
     *                             already exists
     * @throws UserManagerException if there was an error accessing the
     *                              source
     */
    public User createUser(User user)
                throws UserProfileException, UserExistsException,
                       UserManagerException;
    
    /** 
     * updates the information about the user in the source according to
     * the given User object. The User object must be a valid user object,
     * with a valid user Id. Except the fixed fields (Id, CreateDate,
     * LastModified) all fields of the user object can be modified. Which
     * fields shouldn't be allowed to change should decide the application
     * using this method. 
     *
     * @param user the valid User object with the new information which
     *             should be updated
     * @return the updated User object how it was retrieved from the source
     *         after updating
     * @throws UserNotFoundException if there was no user found with the
     *                               provided Id
     * @throws UserManagerException if there was an error accessing the
     *                              source
     * @throws UserProfileException if there was user without an Id
     *                              specified
     */
    public User updateUser(User user)
                throws UserNotFoundException, UserProfileException,
                       UserManagerException;
    
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
     *                              source
     */
    public User[] searchUser(User mask, int limit)
                  throws UserManagerException;
    
    /**
     * deletes the specified user from the source.
     *
     * @param user the User object of the user which should be deleted
     * @throws UserNotFoundException if the user which should be deleted
     *                               cannot be found in source
     * @throws UserManagerException if there was an error performing source
     *                              operations
     * @throws UserProfileException if there is no Id in User object
     *                              defined
     */
    public void deleteUser(User user)
                throws UserNotFoundException, 
                       UserManagerException,
                       UserProfileException;
    
    /**
     * deletes the user with the specified Id from the source.
     *
     * @param id the user's id which should be deleted
     * @throws UserNotFoundException if the user which should be deleted
     *                               cannot be found in source
     * @throws UserManagerException if there was an error performing source
     *                              operations
     * @throws UserProfileException if there is no valid id specified
     */
    public void deleteUser(int id)
                throws UserNotFoundException, 
                       UserManagerException,
                       UserProfileException;
}
