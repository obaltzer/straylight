/* $Id: Document.java,v 1.2 2001/09/27 16:39:55 racon Exp $ */

/* CHANGES
 *
 * 2001-04-22	ob	not given properties will be empty ("") not null
 */
  
package org.pr0.straylight.fw.tp;

import java.util.*;

/**
 * The Document class represents the document which should be genarated in
 * the context of the Velocity engine. It is loaded with default properties
 * and can be modified by simple VTL statements. The defaults would be set
 * by the following properties:
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
 * @version $Revision: 1.2 $ $Date: 2001/09/27 16:39:55 $
 */   
public class Document
{
    private String templateFile = "";
    private String title = "";
    private String author = "";
    private String description = "";
    private String body = "";
    private String root = "";
    private String keywords = "";
    private String date = "";
    
    /**
     * Creates a new Document object with the provided default properties
     * (see above).
     *
     * @param props the default properties of the Document
     */ 
    public Document(Properties props)
    {
        if(props != null)
        {
            templateFile = 
                props.getProperty("Document.defaultTemplate", "");
            title = 
                props.getProperty("Document.defaultTitle", "");
            author = 
                props.getProperty("Document.defaultAuthor", "");
            description =
                props.getProperty("Document.defaultDescription", "");
            keywords = 
                props.getProperty("Document.defaultKeywords", "");
            root = 
                props.getProperty("Document.defaultRoot", "");
            date = 
                props.getProperty("Document.defaultModificationDate", "");
        }
    }
    
    /**
     * Sets the filename of the template to which the Document object
     * should be applied during the last step.
     *
     * @param t the filename anywhere in the resource pathes of Velocity
     */
    public void setTemplate(String t)
    {
        templateFile = t;
    }
    
    /**
     * Returns the filename of the template to which the Document will be
     * applied during the last step.
     */
    public String getTemplate()
    {
        return templateFile;
    }
    
    /**
     * Returns the modification date.
     */
    public String getDate()
    {
        return date;
    }

    /**
     * Sets the keywords belonging to the document content.
     * 
     * @param k the keywords comma separated as a string
     */ 
    public void setKeywords(String k)
    {
        keywords = k;
    }
    
    /**
     * Returns the keywords belonging to the document content.
     */ 
    public String getKeywords()
    {
        return keywords;
    }
    
    /**
     * Sets the description belonging to the document content.
     * 
     * @param d the description as a string
     */ 
    public void setDescription(String d)
    {
        description = d;
    }
    
    /**
     * Returns the description belonging to the document content.
     */ 
    public String getDescription()
    {
        return description;
    }

    /**
     * Returns the author of the document.
     */ 
    public String getAuthor()
    {
        return author;
    }
    
    /**
     * Sets the author of the document.
     *
     * @param a the name/email of the author
     */ 
    public void setAuthor(String a)
    {
        author = a;
    }  
    
    /**
     * Returns the title of the document.
     */ 
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the title of the document.
     *
     * @param t the title
     */ 
    public void setTitle(String t)
    {
        title = t;
    }

    /**
     * Returns the body of the Document.
     */ 
    public String getBody()
    {
        return body;
    }

    /**
     * Sets the body of the document.
     *
     * @param b the body as a string
     */
    public void setBody(String b)
    {
        body = b;
    }

    /**
     * Returns the default URL root of the document.
     */
    public String getRoot()
    {
        return root;
    }
}
