/* $Id: TemplateProcessor.java,v 1.3 2002/02/10 18:53:42 racon Exp $ */

package org.pr0.straylight.fw.tp;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This is the wrapper class which connects to the Velocity Template Engine
 * and processes strings with this engine. It can be used in two modes, an
 * instance and a VM mode. Using in the instance mode means to create an
 * instance of this class and to use the instance methods. The VM mode uses
 * the static methods of this class and just creates a local instance of
 * the Template Processor. In both cases the Template Processor needs to be
 * configured with a property list. Mainly this property list just defines
 * document defaults and looks like:
 * <dl>
 *  <dt><b>Document.defaultTemplate</b></dt>
 *  <dd>
 *   <p>the default template file anywhere in the resource pathes of
 *   velocity</p>
 *  </dd>
 *  <dt><b>Document.defaultTitle</b></dt>
 *  <dd><p>the default title of the document</p></dd>
 *  <dt><b>Document.defaultAuthor</b></dt>
 *  <dd><p>the default author of the document</p></dd>
 *  <dt><b>Document.defaultDescription</b></dt>
 *  <dd>
 *   <p>The default description which can be inserted into META tags</p>
 *  </dd>
 *  <dt><b>Document.defaultKeywords</b></dt>
 *  <dd>
 *   <p>The default keywords which can be inserted into META tags</p>
 *  </dd>
 *  <dt><b>Document.defaultRoot</b></dt>
 *  <dd>
 *   <p>The URL root of the site. Eg: http://www.pr0.org</p>
 *  </dd>
 *  <dt><b>Document.defaultModificationDate</b></dt>
 *  <dd>
 *   <p>The default modification date. It should be the actual date, when
 *   the template processor will be runed.</p>
 *  </dd>
 * </dl>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2002/02/10 18:53:42 $
 */
public class TemplateProcessor
{
    /**
     * the static Template Processor for VM mode use.
     */
    private static TemplateProcessor tp = null;
    
    /** 
     * the configuration properties.
     */
    private Properties props;
    
    /**
     * Initializes the Velocity engine with the properties file given in
     * the property Velocity.ConfigFile. The properties should also contain
     * the default configuration for the later used Document object.
     *
     * @param props the Properties containing the configuration parameter
     * @throws TemplateProcessorException if something goes wrong during
     *         initialization
     */ 
    public TemplateProcessor(Properties props)  
                             throws TemplateProcessorException
    {
        this.props = props;
        
        // the properties for the Velocity template engine
        Properties vprops = new Properties();
        Enumeration propnames = props.propertyNames();
        // extract the properties defined for the velocity template engine
        while(propnames.hasMoreElements())
        {
            String propname = (String)propnames.nextElement();
            String propvalue = (String)props.getProperty(propname);

            if(propname.startsWith("TemplateProcessor"))
            {
                vprops.setProperty(
                    propname.substring(("TemplateProcessor.").length(),
                                       propname.length()),
                    propvalue
                );
            }
        }
        try
        {
            // initalize the Velocity engine with the properties 
            Velocity.init(vprops);
        }
        catch(Exception e)
        {
            throw new TemplateProcessorException(
                "Can not initalize Velocity: "
                + e.getClass().getName() + ": "
                + e.getMessage()
            );
        }
    }

    /**
     * initalize the Template Processor for VM mode usage. It requires a
     * list of properties to configure the Velocity Template Engine and the
     * defaults for documents which will be generated.
     *
     * TODO May be it is useful to provide reinitalization
     * 
     * @param props the configuration properties
     * @throws TemplateProcessorException on configuration errors
     */
    public static void init(Properties props) 
                       throws TemplateProcessorException
    {
        if(tp == null)
        {
            tp = new TemplateProcessor(props);
        }
        else
        {
            throw new TemplateProcessorException(
                "VM Template Processor already "
                + "configured."
            );
        }
    }
    
    /**
     * Processes the input string thru the template engine, after creating
     * a context and adding a Document object to this context. All VTL
     * statements in this string will be processed and than the remaining
     * unparsed content will be used as the Document body. In the last step
     * the Document object will be applied to a template specified in the
     * Document object itselfe. This can be the default or an other via VTL
     * specified template. The return value is a string with the result of
     * the processing.
     *
     * @param input the input content string with or without VTL statements
     * @throws TemplateProcessorException if something goes wrong
     * @return the result of the processing as a String
     */ 
    public String processDocumentString(String input) 
                                        throws TemplateProcessorException
    {
        // creating a new context
        VelocityContext context = new VelocityContext();
     
        // creating a new Document object and use the given properties as
        // defaults
        Document doc = new Document(props);

        // add the Document object to the context
        context.put("Document", doc);
     
        // the string writer for the processed content
        StringWriter newBodyWriter = new StringWriter();
        try
        {
            // modify context with instructions in content file
            Velocity.evaluate(
                context,
                newBodyWriter,
                "ContentString",
                input
            );
        }
        catch(ParseErrorException e)
        {
            // thrown if there was a VTL syntax error
            throw new TemplateProcessorException(
                "VTL syntax error: "
                + e.getMessage()
            );
        }
        catch(MethodInvocationException e)
        {
            // thrown if a method called by a VTL statement throws an
            // exception
            throw new TemplateProcessorException(
                "Exception thrown by " 
                + "method called via VTL: " 
                + e.getMessage()
            ); 
        } 
        catch(Exception e) 
        {
            // catches all unexpected exceptions
            throw new TemplateProcessorException(
                "Unknown exception thrown: "
                + e.getClass().getName() + ": " 
                + e.getMessage()
            ); 
        }

        // set the documents body the processed content string
        doc.setBody(newBodyWriter.toString()); 
        // the writer for the complete result
        StringWriter outputWriter = new StringWriter();
        if(doc.getTemplate() == null) 
        { 
            throw new TemplateProcessorException(
                "No Template defined for this"
                + " context."
            );
        }
        try
        {
            // creating a template with the template file specified in the
            // Document object 
            Template template = Velocity.getTemplate(doc.getTemplate());
      
            // Apply it to the existing kontext
            template.merge(context, outputWriter);
        }
        catch(ResourceNotFoundException e)
        {
            // thrown if template file couldn't be found
            throw new TemplateProcessorException(
                "Cannot find template file '"
                + doc.getTemplate() + "': "
                + e.getMessage()
            );
        }
        catch(ParseErrorException e)
        {
            // thrown if there was a VTL syntax error in the template file
            throw new TemplateProcessorException(
                "VTL syntax error: "
                + e.getMessage()
            );
        }
        catch(Exception e)
        {
            // catches all unexpected exceptions
            throw new TemplateProcessorException(
                "Unknown exception thrown: "
                + e.getClass().getName() + ": "
                + e.getMessage()
            );
        }
     
        // return the result
        return outputWriter.toString(); 
    }
    
    /**
     * Merges a template file with the provided context and returns the
     * result as a String back to the caller. The template file must be
     * located in the resource path of Velocity.
     *
     * @param context a VelocityContext
     * @param templatefile the filename of the template file
     * @return the String which is the result of the merging
     */
    public String mergeContext(VelocityContext context, 
                               String templatefile)
                               throws TemplateProcessorException
    {
        StringWriter outputWriter = new StringWriter();
        try
        {
            // creating a template with the specified template file
            Template template = Velocity.getTemplate(templatefile);
      
            // Apply it to the existing kontext
            template.merge(context, outputWriter);
        }
        catch(ResourceNotFoundException e)
        {
            // thrown if template file couldn't be found
            throw new TemplateProcessorException(
                "Cannot find template file '"
                + templatefile+"': "
                + e.getMessage()
            );
        }
        catch(ParseErrorException e)
        {
            // thrown if there was a VTL syntax error in the template file
            throw new TemplateProcessorException(
                "VTL syntax error: "
                + e.getMessage()
            );
        }
        catch(Exception e)
        {
            // catches all unexpected exceptions
            throw new TemplateProcessorException(
                "Unknown exception thrown: "
                + e.getClass().getName() + ": "
                + e.getMessage()
            );
        }
     
        // return the result
        return outputWriter.toString(); 
    }     
    
    /**
     * This is only a wrapper, which can convert from a Hashtable to a
     * VelocityContext. The advantage is, that the call do not need to
     * create a VelocityContext and can only use a Hashtable.
     *
     * @param context the context as a Hashtable
     * @param templatefile the filename of the templatefile
     * @return the String which is the result of the merging
     */
    public String mergeContext(Hashtable context, String templatefile)
                               throws TemplateProcessorException
    {
        // creating the new VelocityContext
        VelocityContext vcontext = new VelocityContext();
     
        // getting all keys from the Hashtable
        Enumeration keys = context.keys();
     
        // foreach key
        while(keys.hasMoreElements())
        {
            // get the key-name
            String name = (String)keys.nextElement();
            // add the referenced Object to the velocity context
            vcontext.put(name, context.get(name));
        }
     
        // call the mergeContext method and return the result
        return mergeContext(vcontext, templatefile);
    }

    /**
     * creates a complete document from the given context, the given
     * template name and the basetemplate. The document object will be
     * created before the first template is merged with the context, so a
     * modification of the document object is possible by the first
     * template.
     * 
     * TODO speed up this function
     * 
     * @param context the context as a hashtable.
     * @param templatename the name of the template to merge with.
     */
    public String createDocument(Hashtable context, String templatename) 
                                 throws TemplateProcessorException
    {
        // the return value
        String result;

        // convert the Hashtable into a Velocity context
        // creating the new VelocityContext
        VelocityContext vcontext = new VelocityContext();
     
        // getting all keys from the Hashtable
        Enumeration keys = context.keys();
     
        // foreach key
        while(keys.hasMoreElements())
        {
            // get the key-name
            String name = (String)keys.nextElement();
            // add the referenced Object to the velocity context
            vcontext.put(name, context.get(name));
        }
     
        // create the Document object
        Document doc = new Document(props);
        // add the Document object to the context
        vcontext.put("Document", doc);
        // merge context with given template
        result = mergeContext(vcontext, templatename);
        // if for document a template specified
        String basetemplate = doc.getTemplate();
        if(!basetemplate.equals(""))
        {
            // set the body of the document to the formaly merged string
            doc.setBody(result);
            // and merge context with basetemplate
            result = mergeContext(vcontext, basetemplate);
        }
    
        // retrun the result
        return result;
    }

    /**
     * the VM mode equivalent to the createDocument() method.
     *
     * @param context a hashtable containing the context objects
     * @param template the name of the template with which the context
     *                 should be merged
     * @throws TemplateProcessorException on processing errors
     * @return the result of the processing as a string
     */
    public static String createDocument_VM(Hashtable context, 
                                           String templatename)
                         throws TemplateProcessorException
    {
        if(tp != null)
        {
            return tp.createDocument(context, templatename);
        }
        else
        {
            throw new TemplateProcessorException(
                "VM Template Processor is not "
                + "configured. Cannot create "
                + "document."
            );
        }
    }
}
