/* $Id: BaseServlet.java,v 1.3 2001/09/27 16:39:54 racon Exp $ */

package org.pr0.straylight.fw.servlet;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;

/**
 * This class provides a simple basic Application Servlet which provides
 * some usable methods like URI-Parsing, Parameter-Parsing, Properties
 * Loading. It is used as a super class for all Straylight servelts.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a> 
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:54 $
 */
public class BaseServlet extends HttpServlet
{
    /**
     * This flag indicates, if the local configure method was called.
     */
    private boolean localConfig = false;
    
    /**
     * The name of the servlet.
     */
    private String servletname;
    
    /**
     * A flag which is set, if either the configure method of the super
     * class  (this class) or of the Servlet-Implementation detected an
     * error in the configuration.
     */
    private boolean configerror = false;
    
    /**
     * The configuration properties of the servlet.
     */
    private Properties props = null;

    /**
     * This method initalizes the servlet. It will call the local doConfig
     * method, which calls the configuration method.
     *
     * @throws javax.servlet.ServletException
     */
    public void init() throws ServletException
    {
        // setting the servletname for log messages
        servletname = getServletName();
     
        try
        {
            doConfig();
        }
        catch(ConfigurationException e)
        {
            log(e.getMessage());
            cleanup();
            throw new ServletException(
                "There was a configuration error on init. "
                + "Please see the log files for more "
                + "information."
            );
        }
    }

    /**
     * configures the Servlet. You should override it if you need and you
     * have to call this method by 'super.configure();', before you go on
     * with the configuration of your Servlet. This method respectively
     * this of your Servlet will be automatically called by the private
     * goConfig() method of this Superclass. It will/should also checks the
     * configuration of the servlet, so we prevent errors during runtime.
     * This method should be overwritten if the subclass uses
     * configurations which can produce fatal errors for Servlet if they
     * are wrong. 
     */
    public void configure() 
    {
        ServletConfig config = getServletConfig();

        Properties props = new Properties();
        String propsfilename;
        // if there is a property file specified, try to load it 
        if((propsfilename = config.getInitParameter("PropertiesFile")) 
            != null)
        {
            log("Try to load properties file: " + propsfilename);
            Properties tmpprops = new Properties();
            try
            {
                // try to load the properties file
                tmpprops.load(new FileInputStream(
                    new File(propsfilename)
                ));
                log("Properties file successfully loaded.");
            }
            catch(IOException e)
            {
                configerror = true;
                log("ERROR: Cannot read properties file '" 
                    + propsfilename + "'.");
            }
      
            // copy properties to the servlet property object
            Enumeration propnames = tmpprops.propertyNames();
            while(propnames.hasMoreElements())
            {
                // get the property name
                String propname = (String)propnames.nextElement();
                // and the property value
                String propvalue = tmpprops.getProperty(propname);
       
                // load all properties and cut servlet name from beginning
                // that also overwrites standard properties with servlet 
                // specific
                if(propname.startsWith(servletname + ".")) 
                {
                    // cut the leading servletname
                    propname = propname.substring(
                        (servletname + ".").length(),
                        propname.length()
                    );
                }
       
                // insert init property into servletproperties
                System.out.println(propname + ": " + propvalue);
                props.setProperty(propname, propvalue);
            }
        }
     
        // overwrite properties from config file with properties from init
        // parameter
      
        // get initalization parameter names
        Enumeration paramnames = config.getInitParameterNames();
    
        // foreach parameter name
        while(paramnames.hasMoreElements())
        {
            // get the parameter name
            String paramname = (String)paramnames.nextElement();
            // and the parameter value
            String paramvalue = config.getInitParameter(paramname);
            // insert init parameters for servlet into properties list
            System.out.println(paramname + ": " + paramvalue);
            props.setProperty(paramname, paramvalue);
        }
     
        this.props = props;
        localConfig = true;
    }
    
    /**
     * Clean up the configuration for reloading. You should override this
     * method if you also have to clean up something befor reconfigure. 
     */
    public void cleanup()
    {
        this.props = null;
    }
    
    /**
     * This is only a private wrapper method, to make sure that we don't
     * forget to call your counfigure(); method and of course not the
     * important local configure method. It will throw a
     * ConfigurationException if the 'configerror' flag is set after runnig
     * the configure methods.
     *
     * @throws org.pr0.straylight.servlets.ConfigureException
     */
    private void doConfig() throws ConfigurationException
    {
        // call your configure method
        configure();
     
        if(!localConfig)
        {
            throw new ConfigurationException(
                "The local configure(); method "
                + "wasn't called by the childclass. "
                + "(You have to do it by calling "
                + "super.configure(); on the top of "
                + "your configure(); method.) I'll "
                + "stop here to prevent errors. :-("
            );
        }   
        if(configerror)  
        {
            configerror = false;
            throw new ConfigurationException(
                "The application/servlet cannot "
                + "be started correctly. There was "
                + "one or more configuration error. " 
                + "See above."
            );
        }
    }

    /**
     * reloads the configuration during runtime. If a
     * ConfigurationException was thrown it will log it and send a
     * ServletException to the Servlet Container.
     *
     * @throws javax.servlet.ServletException
     */
    public void reinit() throws ServletException
    {
        log("The configuration will be reloaded. Cleaning up...");
        try
        {
            cleanup();
            doConfig();
        }
        catch(ConfigurationException e)
        {
            log("ERROR: " + e.getMessage());
            cleanup();
            
            throw new ServletException(
                "There was a configuration error on reinit. "
                + "Please see the log files for more "
                + "information."
            );
        }
        log("The configuration was successfully reloaded :-)");
    }  
    
    /**
     * sets the configerror flag to true. You should call this method if
     * you had an error in the configuration you used to check.
     */
    public void foundError()
    {
        configerror = true;
    }
    
    /**
     * returns the properties table.
     */
    public Properties getProperties()
    {
        return this.props;
    }
    
    /**
     * cleans everything up.
     */
    public void destroy()
    {
        cleanup();
    }
}
