/* $Id: DBConnectionPool.java,v 1.2 2001/09/27 16:39:53 racon Exp $ */

package org.pr0.straylight.fw.db;

import java.sql.SQLException;
import java.util.Properties;

/**
 * This class provides a pool/cache of open database connections. This
 * connections can be fetched by applications, which need a fast access to
 * a database without the overhead of opening a connection. The pool is
 * completely thread-saved designed, so it is possible to access one pool
 * by many parallel running threads. There is also an observer running
 * during the DBConnectionPool is used, which checks for connection which
 * are in use for a too long time, connections which are unused for a too
 * long time and creates unused open connections to held them in the pool
 * for requests. This class can be used in two modes, an instance mode and
 * a VM mode. In the instance mode you create an instance of this class and
 * accesses the functions for fetching and releasing database connections.
 * In the VM mode you can run the static method init() which creates an
 * instance of the DBConnectionPool itself and you can then use the static
 * functions the fetch and release connections. The constructor and the
 * init method need a list of properties with which the Database Connection
 * Pool should be initalized.
 *
 * Possible properties are:
 * <dl>
 *  <dt>DBConnectionPool.DriverClass</dt>
 *  <dd>
 *   the classname of the JDBC driver <i>(required)</i>
 *  </dd>
 *  <dt>DBConnectionPool.URL</dt>
 *  <dd>
 *   the URL which should be used to create a connection to a database
 *   <i>(required)</i>
 *  </dd>
 *  <dt>DBConnectionPool.Username</dt>
 *  <dd>
 *   the username of the database user <i>(optional)</i>
 *  </dd>
 *  <dt>DBConnectionPool.Password</dt>
 *  <dd>
 *   the password of the database user <i>(required if username is
 *   set)</i>
 *  </dd>
 *  <dt>DBConnectionPool.UsedTimeout</dt>
 *  <dd>
 *   the time in seconds, how long a connection can be in use
 *   <i>(optional:
 *   default 1800, 0 to disable)</i>
 *  </dd>
 *  <dt>DBConnectionPool.UnusedTimeout</dt>
 *  <dd>
 *   the time in seconds, how long a connection can be open but unused 
 *   <i>(optional: default 600, 0 to disable)</i>
 *  </dd>
 *  <dt>DBConnectionPool.MinUnused</dt>
 *  <dd>
 *   the number of unused open connections which should be at least in
 *   cache
 *   <i>(optional: default 1)</i>
 *  </dd>
 *  <dt>DBConnectionPool.MaxConnections</dt>
 *  <dd>
 *   the number of maximum connection which can be holded in the cache at
 *   the same time <i>(optional: default 10)</i>. If this value is reached
 *   and a request for a connection occures while all connections are in
 *   use, the caller will be blocked until a connection is freed. 
 *  </dd>
 *  <dt>DBConnectionPool.ObserverIntervall</dt>
 *  <dd>
 *   the number of milliseconds between two runnings of the observer
 *   <i>(absolutely optional, should only be used by cracks, default
 *   5000)</i>
 *  </dd>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:53 $
 */
public class DBConnectionPool extends Thread
{
    /**
     * The instance of the DBConnectionPool for the VM mode.
     */
    private static DBConnectionPool dbpool = null;
 
    /**
     * The list of the connections.
     */
    private DBConnection[] connections;
 
    /**
     * The array which marks a particular connection as unused, if the
     * special field in the array is filled with a timestamp.
     */
    private long[] unusedTime;

    /**
     * The array which marks a particular connection as used, if the
     * special field in the array is filled with a timestamp.
     */
    private long[] usedTime;
 
    /**
     * The intervall in milliseconds, between two runs of the observer.
     */
    private long observerIntervall;
 
    /**
     * The timestamp counter. Will be increased by the observer.
     */
    private long tstamp;
 
    /**
     * Configuration value for the timeout of used connections.
     */
    private long usedTimeout;

    /**
     * Configuration value for the timeout of unused connections.
     */
    private long unusedTimeout;
 
    /**
     * The maximum number of connections which can be open at the same
     * time.
     */
    private int maxConnections;
 
    /**
     * The number of unused connections, pool should have at any time. 
     */
    private int minUnused;
 
    /**
     * The URL which is used to connect the database.
     */
    private String url;
 
    /**
     * The username which is used to connect the database.
     */
    private String username;
 
    /**
     * The password which is used to connect to the database.
     */
    private String password;
 
    /**
     * The classname for the JDBC Database Driver.
     */
    private String driverclass;
 
    /**
     * A control variable to stop the observer if running.
     */
    private boolean runObserver = false;
  
    /**
     * This constructor initalizes the DBConnectionPool and starts the
     * thread observing the pool. It expects a list of properties to set up
     * the pool and know how to access the database.
     * 
     * @param props the configuration properties for the pool
     * @throws DBConnectionPoolException on configuration error
     */
    public DBConnectionPool(Properties props) 
                            throws DBConnectionPoolException
    {
        // call super class constructor
        super();

        // check if properties are initalized
        if(props == null)
        {
            throw new DBConnectionPoolException(
                "No propeties specified."
            );
        }
        try
        {
            // loading properties
            // get the number of maximum connections, default is 10
            maxConnections = Integer.parseInt(
                props.getProperty("DBConnectionPool.MaxConnections", "10")
            ); 
   
            // get the driverclass name
            driverclass = 
                props.getProperty("DBConnectionPool.DriverClass");
            
            if(driverclass == null)
            {
                // it is null so we can't proceed
                throw new DBConnectionPoolException(
                    "Error in properties. No JDBC "
                    + "driver class was specified."
                );
            }
       
            // get the database connection url
            url = props.getProperty("DBConnectionPool.URL");
            if(url == null)
            {
                throw new DBConnectionPoolException(
                    "Error in properties. "
                    + "No database connection URL "
                    + "was specified."
                );
            }

            username = props.getProperty("DBConnectionPool.Username");
            password = props.getProperty("DBConnectionPool.Password");
       
            // only proceed if both is given
            if(username != null && password == null)
            {
                throw new DBConnectionPoolException(
                    "Error in properties. Username, "
                    + "but no password specified."
                );
            }
       
            // get the usedTimeout, default 1800000 ms = 30 min
            usedTimeout = Long.parseLong(
                props.getProperty("DBConnectionPool.UsedTimeout", "1800")
            ) * 1000; // make milliseconds

            // get the unusedTimeout, default 600000 ms = 10 min
            unusedTimeout=Long.parseLong(
                props.getProperty("DBConnectionPool.UnusedTimeout", "600")
            ) * 1000; // make milliseconds

            // get minUnused, default 1
            minUnused = Integer.parseInt(
                props.getProperty("DBConnectionPool.MinUnused", "1")
            );
       
            // get observer intervall, default 5 s
            observerIntervall = Long.parseLong(
                props.getProperty(
                    "DBConnectionPool.ObserverIntervall", 
                    "5000"
                )
            );
        }
        catch(NumberFormatException e)
        {
            // thrown if one of the numeric values were not nummeric
            throw new DBConnectionPoolException(
                "Error in properties. No number, "
                + "where number expected."
            );
        }
      
        // loading driver class now  
        try
        {
            Class.forName(driverclass);
        }
        catch(ClassNotFoundException e)
        {
            // the driver class is not accessable
            throw new DBConnectionPoolException(
                "Error in properties. "
                + "Cannot load JDBC driver class: "
                + driverclass
            );
        }
      
        // initalize connection field  
        connections = new DBConnection[maxConnections];
        unusedTime = new long[maxConnections];
        usedTime = new long[maxConnections];
      
        // initalize the timestamp
        tstamp = 0;
      
        // start the observer
        runObserver = true;
        this.start();  
        
        // DBConnectionPool is now ready for work
    }

    /**
     * initalize the database connection pool for use as a static VM wide
     * connection pool which can be shared by all objects currently in the
     * VM. It also requires a properties list with the same information
     * like in the standard constructor, because this method will create a
     * local instance of an instance database connection pool and redirect
     * the static method calls to this instance object.
     *
     * @param props the configuration properties
     * @throws DBConnectionPoolException on configuration error
     */
    public static void init(Properties props) 
                            throws DBConnectionPoolException
    {
        if(dbpool == null)
        {
            dbpool = new DBConnectionPool(props);
        }
        else
        {
            throw new DBConnectionPoolException(
                "VM Database Connection Pool "
                + "is already configured and "
                + "running.");
        }
    }

    /**
     * returns the local instance of the Database Connection Pool in VM
     * mode only otherwise null.
     *
     * @return the instance of the database connection pool
     */
    public static DBConnectionPool getInstance()
    {
        return dbpool;
    }
    
    /**
     * It's providing the observer functionality. Runs in background as a
     * parallel thread and as long the local flag runObserver is set to
     * true it will increase the timestamp, runs the timeout check, runs
     * the check for a minimum of unused connections and sleeps for the
     * specified intervalperiod. It can be interrupted in sleeping by the
     * call of the interrupt-method of this object, like done in the
     * cleanUp-method.
     */
    public void run()
    {
        // as long runObserver is true
        while(runObserver)
        {
            try
            {
                // increase tstamp
                tstamp += observerIntervall;
       
                // check the timeouts
                checkTimeouts(); 
                // check the minimum connections
                checkMinUnused();
                // sleep for the specified time
                sleep(observerIntervall);
            }
            catch(DBConnectionPoolException e)
            {
                System.err.println(e.getMessage());
                System.err.println(
                    "Fatal DBConnectionPool Error. Exiting Observer."
                );
                runObserver = false;
            }
            catch(InterruptedException e)
            {
                /* Ignore this exception, probably I should finalize */
            }
        }
    }
    
    /**
     * creates a new connection. If there is no place to create the
     * connection, no connection will be created and the method will
     * return.
     *
     * @throws org.pr0.straylight.database.DBConnectionPoolException 
     *         when SQLException was thrown by trying open the connection
     */ 
    private synchronized void createConnection() 
                              throws DBConnectionPoolException
    {
        // exclusive access to connections-array
        synchronized(connections)
        {
            // exclusive access to unusedTime array
            synchronized(unusedTime)
            {
                // exclusive access to usedTime array
                synchronized(usedTime)
                {
                    // free connection found
                    boolean found = false;

                    // connection counter 
                    int i = 0;

                    // as long no free connection found and there are more
                    // in field
                    while(!found && i < maxConnections)
                    {
                        // if connection == null than is free
                        if(connections[i] == null) 
                            found = true;
                        else 
                            i++;
                    }

                    // only if free field found
                    if(found == true)
                    {
                        try
                        {
                            if(username != null)
                            {
                                connections[i] = new DBConnection(
                                    url,
                                    username,
                                    password
                                );
                            }
                            else
                            {
                                connections[i] = new DBConnection(url);
                            }
                            // unused since now
                            unusedTime[i] = tstamp;
                            // the new created connection is an unused
                            usedTime[i] = -1;
                            // we have a new unused connection, why not
                            // waking up waiting threads?
                            this.notify();
                        }
                        catch(SQLException e)
                        {
                            throw new DBConnectionPoolException(
                                "Cannot create database "
                                + "connection. SQLException: "
                                + e.getMessage()
                            );
                        }
                    }
                    else
                    {
                        // this is nonblocking
                    }
                } // unlocking usedTime
            } // unlocking unusedTime
        } // unlocking connections
    }
    
    /**
     * checks if the minimum of unused connections is open. If there are
     * less unused connections than specified in minUnused, it tries to
     * create the rest connections.
     *
     * @throws org.pr0.straylight.database.DBConnectionPoolException when
     *         createConnection was throwing one
     */
    private void checkMinUnused() throws DBConnectionPoolException
    {
        int countunused = 0;
        int i = 0;
        // count as long as we found enough unused connections or reached
        // the maximum count of connections
        while(countunused < minUnused && i < maxConnections)
        {
            // if this an unused connection count it
            if(connections[i] != null && unusedTime[i] != -1) 
                countunused++;
            i++;
        }
        if(countunused < minUnused)
        {
            // if the count of unused connections smaller than 
            // it should be, calculate the difference
            int tocreate = minUnused - countunused;
            // and try create as many connection we need
            for(i = 0; i < tocreate; i++)
                createConnection();
        }
    }
    
    /**
     * deletes the specified connection in the array. That means it will
     * try to close the database connection and set the array position to
     * null. If closing the database connection will not work, it is the
     * task of the garbage collector to free this connection.
     *
     * @param i the number ob the connection to be deleted
     */
    private synchronized void deleteConnection(int i)
    {
        // exclusive access to connections-array
        synchronized(connections)
        {
            // exclusive access to unusedTime array
            synchronized(unusedTime)
            {
                // exclusive access to usedTime array
                synchronized(usedTime)
                {
                    // if the given connection is open
                    if(connections[i] != null)
                    {
                        try
                        {
                            // try to close the connection
                            connections[i].getConnection().close();
                        }
                        catch(SQLException e)
                        {
                            // ignore it and set connection[i] to
                            // null...let it do by the garbage collector, I
                            // have no time for handling it 
                            // throw new
                            // DBConnectionPoolException("Cannot close
                            // database connection.");
                        }
                        // mark the fields as unused
                        connections[i] = null;
                        unusedTime[i] = -1;
                        usedTime[i] = -1;
                    }
                    else
                    {
                        // if already no connection defined 
                        // make sure that the other fields are also unused
                        unusedTime[i] = -1;
                        usedTime[i] = -1;
                    }
                }
            }
        }
    }
         
    /**
     * checks the timeouts of the connections in the pool.
     */ 
    private void checkTimeouts()
    {
        for(int i = 0; i < maxConnections; i++)
        {
            // force deleting used (probably a hanged thread) and unused
            // connection if their timeout is reached, bot only if timeout
            // set
            if(connections[i] != null)
            {
                if(usedTime[i] != -1 && usedTimeout != 0)
                {
                    if((tstamp - usedTime[i]) > usedTimeout)
                    {
                        deleteConnection(i);
                    }
                }
                else if(unusedTime[i] != -1 && unusedTimeout != 0)
                {
                    if((tstamp - unusedTime[i]) > unusedTimeout)
                    {
                        deleteConnection(i);
                    }
                }
            }
        }
    }  
          
    /**
     * get a connection from the pool. This method is blocking for the
     * caller, if no connection is available. If no open unused connection
     * was available in pool, but there is place for a new one, it will be
     * created.
     *
     * @throws org.pr0.straylight.database.DBConnectionPoolException when a
     *         SQLException was thrown by opening a new connection
     */
    public synchronized DBConnection getConnection() 
                                     throws DBConnectionPoolException
    {
        boolean done = false;
        DBConnection retval = null;
     
        while(!done)
        {
            // exclusive access to connections-array
            synchronized(connections)
            {
                // exclusive access to unusedTime array
                synchronized(unusedTime)
                {
                    // exclusive access to usedTime array
                    synchronized(usedTime)
                    {
        
                        // it is set to true if we found an unused
                        // connection
                        boolean found = false;
                        // only the counter thru the pool
                        int i = 0;
                        // the position, where a connection could be
                        // created
                        int freepos = -1;

                        // looking for free connections, or free positions
                        // for new connections
                        while(!found && i < maxConnections)
                        {
                            if(connections[i] == null)
                            {
                                // actual position is a free position
                                freepos = i;
                            }
                            if(connections[i] != null && usedTime[i] == -1)
                            {
                                // wow, we found a unused connection
                                found = true;
                            }
                            else
                            {
                                // no, no unused connection here, look at
                                // next position
                                i++;
                            }
                        }
         
                        if(found == true)
                        {
                            // so we found an unused connection, let's take
                            // this
                            retval = connections[i];
                            // set it to used
                            usedTime[i] = tstamp;
                            unusedTime[i] = -1;
                            // everything is done
                            done = true;
                        }
                        // ok nothing found, but maybe we found a free
                        // position, to create a connection
                        else if(freepos != -1)
                        {
                            try
                            {
                                if(username != null)
                                {
                                    // create a connection with username
                                    // and password
                                    connections[freepos] = 
                                        new DBConnection(
                                            url, 
                                            username, 
                                            password
                                        );
                                }
                                else
                                {
                                    // create a connection without password
                                    connections[freepos] = 
                                        new DBConnection(url);
                                }
                            }
                            catch(SQLException e)
                            {
                                // shit, this isn't working
                                throw new DBConnectionPoolException(
                                    "Cannot create database "
                                    + "connection. SQLException: "
                                    + e.getMessage()
                                );
                            }
                            // why, we should create a connection if we
                            // don't use it
                            unusedTime[freepos] = -1;
                            // used since now
                            usedTime[freepos] = tstamp;
                            // return this connection 
                            retval = connections[freepos]; 
                            // all done
                            done = true; 
                        }
                        else
                        {
                            // No free position and also no unused
                            // connection found, so we have to wait for a
                            // notify from either createConnection or
                            // realeaseConnection.  But we will not wait
                            // here, because the pool is locked at the
                            // moment, so we will never receive a notify,
                            // because all these methods will be blocked.
                            // Let's do it outside.
                        }       
                    } // unlock usedTime
                } // unlock unusedTime
            } // connections
            if(!done)
            {
                try
                {
                    // The calling thread waits here until an other thread
                    // called releaseConnection or the observer created new
                    // connections with createConnection. After that our
                    // chances are good to get an unused connection.  All
                    // other threads, wants to call getConnection will be
                    // blocked by the VM, because of the synchronized
                    // declaration of the whole method.
                    // System.out.println("Block caller...");
                    this.wait();
                }
                catch(InterruptedException e) 
                {     
                    // The waiting Thread was interrupted by something
                    // else, so I can forget the request for a connection
                    // and return null.
                    done = true;
                }
            }
        }  
        return retval;
    }     
    
    /**
     * the VM mode equivalent for the getConnection method.
     *
     * @throws DBConnectionPoolException if it was not possible to get a
     *                                   free connection
     * @return a free/open database connection
     */
    public static DBConnection getConnection_VM() 
                               throws DBConnectionPoolException
    {
        if(dbpool != null) 
        {
            return dbpool.getConnection();
        }
        else
        {
            throw new DBConnectionPoolException(
                "VM Database Connection Pool is "
                + "not configured.i Cannot get "
                + "connection."
            );
        }
    }
    
    /**
     * simply marks a formaly used connection as unused.
     * TODO currently a connection is only released if the developer calls
     *      this function otherwise the connection will be in use until it
     *      gets to old and the observer will force to destroy this
     *      connection. If it is possible the connection should be
     *      automatically released if no other reference except from this
     *      class is on the connection.
     *
     * @param dbcon the database connection which should be released
     */
    public synchronized void releaseConnection(DBConnection dbcon)
    {
        synchronized(connections)
        {
            // exclusive access to unusedTime array
            synchronized(unusedTime)
            {
                // exclusive access to usedTime array
                synchronized(usedTime)
                {
                    int i = 0;
                    // find the connection to release
                    while(i < maxConnections && connections[i] != dbcon) 
                        i++;
        
                    if(i < maxConnections)
                    {
                        // this connection is not longer in use
                        unusedTime[i] = tstamp;
                        usedTime[i] = -1;
                    }
                }
            }
        }
        // notify waiting threads, that there was a connection released
        // which is free now.
        this.notify();
    }

    /**
     * the VM mode equivalent method to releaseConnection()
     *
     * @param dbcon the database connection which should be released
     */
    public static void releaseConnection_VM(DBConnection dbcon)
    {
        if(dbpool != null) 
            dbpool.releaseConnection(dbcon);
    }
    
    /**
     * cleans up the complete pool and deletes all connections.
     */
    public void cleanUp()
    {
        // tell the observer to stop
        runObserver = false;
        // interrupt the observer if it is sleeping
        this.interrupt();
        for(int i = 0; i < maxConnections; i++)
        {  
            // delete each singe connection
            this.deleteConnection(i);
        }
        connections = null;
        unusedTime = null;
        usedTime = null;
    }

    /**
     * the VM mode equivalent method to cleanUp
     */
    public static void cleanUp_VM()
    {
        if(dbpool != null) 
            dbpool.cleanUp();
    }
    
    /**
     * If the garbage collector deletes this object this method will
     * hopefully called.
     */ 
    protected void finalize()
    {
        cleanUp();
    } 
}
