/* $Id: ApplyTemplateTask.java,v 1.2 2002/02/10 18:54:51 racon Exp $ */

package org.pr0.straylight.ssb;

import org.pr0.straylight.fw.tp.TemplateProcessor;
import org.pr0.straylight.fw.tp.TemplateProcessorException;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.FlatFileNameMapper;
import org.apache.tools.ant.util.FileNameMapper;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * <p>
 * This class implements an Ant task, which is used to apply templates to
 * static files. It is based on the Copy task from the Ant project.  It
 * will be run from an Ant build file, which is runned during the
 * republishing of the static content. As parameters are either a single
 * source and destination file or a destination directory and a fileset
 * required. Optional a file mapper can be set to map the source filenames
 * to different destination filenames, the <tt>remake</tt> attribute can be
 * set to force the remake of each file and a <tt>flatten</tt> flag can be
 * set to store all processed files into one directory.  All the files
 * which should be processed will be reads one after an other from the Task
 * and passed thru the template processor as a string. The included files
 * will be specified by an Ant typical fileset if mutiple files should be
 * processed. See Ant documentation for more. The string with the results
 * of the template processing will be stored in a file with the same like
 * the source file or a new mapped filename in the destination directory.
 * The parameter <tt>remake</tt> is optional and indicates if modification
 * date checkings should be skipped. Set it to <tt>true</tt>, <tt>on</tt>
 * or <tt>yes</tt> if you want to skip date checkings.</p> 
 * <b>An example:</b>
 * <pre>
 *  &lt;taskdef 
 *    name=&quot;applytemplate&quot; 
 *    classname=&quot;org.pr0.straylight.publishing.ant.ApplyTemplateTask&quot;
 *  /&gt;
 *  &lt;applytemplate 
 *    srcfile=&quot;repostitory/html/file.html&quot;
 *    destdir=&quot;/services/wwwdocs&quot;
 *    remake=&quot;yes&quot;
 *  /&gt;
 * </pre>
 * <p>
 *  or
 * </p>
 * <pre>
 *  &lt;taskdef 
 *    name=&quot;applytemplate&quot; 
 *    classname=&quot;org.pr0.straylight.publishing.ant.ApplyTemplateTask&quot;
 *  /&gt;
 *  &lt;applytemplate 
 *    destdir=&quot;/services/wwwdocs&quot;
 *  &gt;
 *   &lt;fileset dir=&quot;content-sources/html"&gt;
 *    &lt;include name=&quot;&#42;&#42;/&#42;.html&quot;/&gt;
 *   &lt;/fileset&gt;
 *  &lt;/applytemplate&gt;
 * </pre>
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.2 $ $Date: 2002/02/10 18:54:51 $
 */
public class ApplyTemplateTask extends Task 
{
    private File srcFile = null; 
    private File destFile = null; 
    private File destDir = null;
    private Vector filesets = new Vector();

    private boolean remake = false;
    private boolean flatten = false;
 
    private Hashtable fileMap = new Hashtable();
    private Mapper mapperElement = null;

    private TemplateProcessor tp = null;
 
    /**
     * Sets a single content source file to process.
     *
     * @param file File object of the source file
     */
    public void setSrcfile(File file) 
    {
        this.srcFile = file;
    }

    /**
     * Sets the destination file for a single processed source file.
     *
     * @param destFile File object of the destination file
     */
    public void setDestfile(File destFile) 
    {
        this.destFile = destFile;
    }

    /**
     * Sets the destination directory for mutiple processed source files.
     *
     * @param destDir File object of the destination directory
     */
    public void setDestdir(File destDir) 
    {
        this.destDir = destDir;
    }

    /**
     * Sets the remake, which indicates if a file should be processed, even if
     * the modification date of the source is older than the one of the
     * destination file.
     *
     * @param remake the indicating boolean value 
     */
    public void setRemake(boolean remake) 
    {
        this.remake = remake;
    }

    /**
     * When processing directory trees, the processed files can be 
     * "flattened" into a single directory.  If there are multiple 
     * files with the same name in the source directory tree, only 
     * the first file will be processed into the "flattened" directory, 
     * unless the remake attribute is true.
     *
     * @param flatten the indicating boolean value
     */
    public void setFlatten(boolean flatten) 
    {
        this.flatten = flatten;
    }

    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param set the FileSet object
     */
    public void addFileset(FileSet set)
    {
        filesets.addElement(set);
    }

    /**
     * Defines the FileNameMapper to use (nested mapper element).
     */
    public Mapper createMapper() throws BuildException 
    {
        if(mapperElement != null) 
        {
            throw new BuildException("Cannot define more than one"
                                     + " filename mapper.", location);
        }
        mapperElement = new Mapper(project);
     
        return mapperElement;
    }

    /**
     * Performs the processing operations.
     */
    public void execute() throws BuildException 
    {
        // validating the given attributes
        validateAttributes();   

        // deal with the single file
        if(srcFile != null) 
        {
            if(srcFile.exists())
            {
                if(destFile == null)
                    destFile = new File(destDir, srcFile.getName());
                
                // is source file newer than destination file
                if(remake 
                    || (srcFile.lastModified() > destFile.lastModified()))
                {
                    // create a file map with only one entry
                    fileMap.put(srcFile,destFile);
                }
            } 
            else
            {
                log("Could not find file '" + srcFile.getAbsolutePath()
                    + "' for processing.");
            }
        }

        // add also all fileset entries to the fileMap
        for(int i = 0; i < filesets.size(); i++)
        {  
            FileSet fs = (FileSet)filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File fromDir = fs.getDir(project);

            String[] srcFiles = ds.getIncludedFiles();
            //   String[] srcDirs=ds.getIncludedDirectories();

            createMap(fromDir, destDir, srcFiles);
        }
     
        // process all files in created map
        processTemplates();
        if(destFile != null)
        {
            destDir = null;
        }
    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set
     * any internal flags necessary based on different combinations 
     * of attributes.
     */
    private void validateAttributes() throws BuildException 
    {
        // check if source file or dir is specified
        if(srcFile == null && filesets.size() == 0) 
        {
            throw new BuildException("No source specified.", location);
        }
     
        // check if source file is a directory, if yes a fileset should be
        // used
        if(srcFile != null && srcFile.exists() && srcFile.isDirectory())
        {
            throw new BuildException("Directories only can performed"
                                     + " using a fileset.", location);
        }

        // check if destination file and dir specified
        if(destFile != null && destDir != null)
        {
            throw new BuildException("You can only specify either a "
                                     + "destination file or a destination"
                                     + " directory.", location);
        }
     
        // check if destination file or directory specified
        if(destFile == null && destDir == null)
        {
            throw new BuildException("You must set at least"
                                     + " a destination file or a "
                                     + "destination"
                                     + " directory.", location);
        }

        // check for mutiple sources and only one destination file
        if(destFile != null && filesets.size() > 0) 
        {
            throw new BuildException("Cannot store all processed"
                                     + " files in one destination file.",
                                     location);
        }
     
        // get destination directory from destination file
        if(destFile != null)
        {
            destDir = new File(destFile.getParent());
        }
    }

    /**
     * Compares source files to destination files to see if they should be
     * copied and creates a map with all filenames to copied and the
     * equivalent mapped destination filenames.
     * 
     * @param fromDir the File object of the source directory
     * @param toDir the File object of the destination directory
     * @param files all filenames of files which were found in source
     *              directory
     */
    private void createMap(File fromDir, File toDir, String[] files) 
    {
        FileNameMapper mapper = null;
        String[] toCopy = null;
     
        // find the file mapper to use
        if(mapperElement != null)
        {            
            // use the specified one
            mapper = mapperElement.getImplementation();
        }
        else if(flatten)
        {
            // use a flatten mapper
            mapper = new FlatFileNameMapper();
        }
        else
        {
            // use a standard mapper
            mapper = new IdentityMapper();
        }
     
        if(remake)
        {
            // process each filename
            Vector v = new Vector();
            for(int i = 0; i < files.length; i++)
            {
                // remap the given filenames
                if(mapper.mapFileName(files[i]) != null)
                {
                    v.addElement(files[i]);
                }
            }
     
            // create an Array of the filenames matching the map
            toCopy = new String[v.size()];
            v.copyInto(toCopy);
        }
        else
        {
            // only get filenames where the source is newer than the
            // destination
            SourceFileScanner ds = new SourceFileScanner(this);
            toCopy = ds.restrict(files, fromDir, toDir, mapper);
        }
     
        // put the files into a map
        for(int i = 0; i < toCopy.length; i++)
        {
            File src = new File(fromDir, toCopy[i]);
            File dest = new File(toDir, mapper.mapFileName(toCopy[i])[0]);
            fileMap.put(src, dest);
        }
    }
    
    /**
     * Do the template processing for each file in the file map.
     */
    private void processTemplates() 
    {
        if(fileMap.size() > 0)
        {
            log("Processing " + fileMap.size()
                + " file" + (fileMap.size() == 1 ? "" : "s")
                + " to " + destDir.getAbsolutePath());
      
            try
            {
                // creates a new TemplateProcessor
                tp = new TemplateProcessor(
                    makeProps(project.getProperties()));
            }
            catch(TemplateProcessorException e)
            {
                // the template processor couldn't be initialized
                throw new BuildException("Cannot create template "
                                         + "processor: " + e.getMessage());
            }
      
            Enumeration srcNames = fileMap.keys();
            while(srcNames.hasMoreElements())
            {
                File fromFile = (File)srcNames.nextElement();
                File toFile = (File)fileMap.get(fromFile);
                if(fromFile.equals(toFile))
                {
                    log("Skipping processing of '"
                        + fromFile.getAbsolutePath()
                        + "' to itself.");
                }
                else
                {
                    try
                    {
                        applyTemplate(fromFile, toFile);
                    }
                    catch(IOException e)
                    {
                        // exception during reading or writing from/to file
                        throw new BuildException(
                            "IO Exception "
                            + "From: " + fromFile.getAbsolutePath() + " "
                            + "To: " + toFile.getAbsolutePath() + " "
                            + e.getMessage()
                        );
                    }
                    catch(TemplateProcessorException e)
                    {
                        // template processor throw exception
                        throw new BuildException(
                            "TemplateProcessorException "
                            + "From: " + fromFile.getAbsolutePath() + " "
                            + "To: " + toFile.getAbsolutePath() + " " 
                            + e.getMessage()
                        );
                    }     
                }
            }
        }
    }

    /**
     * Reads the source file into a string and process it through the
     * TemplateProcessor. The string returned from the template
     * processor would be written into the destination file.
     *
     * @param sourceFile File object of the source file.
     * @param destFile File object of the destination file.
     * @param remake should the last modification date be checked?
     */
    private void applyTemplate(File sourceFile, File destFile) 
                               throws IOException, 
                                      TemplateProcessorException
    {
        // log to project log
        log("Process: " + sourceFile.getAbsolutePath() + " > "
                        + destFile.getAbsolutePath());

        // create a File object of the directory the destination file should
        // be copied into
        File parent = new File(destFile.getParent());

        // if the directory does not exists creates the directory structure
        // recursivly  
        if(!parent.exists()) 
        {
            parent.mkdirs();
        }

        // open source file for reading
        BufferedReader in = new BufferedReader(new FileReader(sourceFile));
        StringBuffer sbuf = new StringBuffer();
        String line;

        // reads the complete source file
        while((line = in.readLine()) != null)
        {
            sbuf.append(line + "\n");
        }

        // close the source file
        in.close();

        String inputString = sbuf.toString();

        // yeah here is the interesting point
        // process the string
        String outputString = tp.processDocumentString(inputString);

        // open destination file for writing
        BufferedWriter out = new BufferedWriter(new FileWriter(destFile));

        // write the string to file
        out.write(outputString, 0, outputString.length());
        out.flush();
        out.close();
    }

    /**
     * Converts a Hashtable into a properties table. It is
     * necessary because the Ant project only returns a Hashtable
     * of all properties.
     *
     * @param tab the Hashtable to be converted
     */
    private Properties makeProps(Hashtable tab)
    {
        Properties props = new Properties();
        if(tab != null)
        {
            Enumeration keys = tab.keys();
            while(keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
       
                props.setProperty(key, (String)tab.get(key));
            }
        }
        return props;
    }
}
