/* $Id: ShowMessageListRequest.java,v 1.3 2001/09/27 16:39:50 racon Exp $ */

package org.pr0.straylight.fs;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Category;
import org.pr0.straylight.fw.db.DBConnection;
import org.pr0.straylight.fw.db.DBConnectionPoolException;
import org.pr0.straylight.fw.util.SimpleDate;
import org.pr0.straylight.fw.util.StringFilterChain;
import org.pr0.straylight.fw.util.TextToHTMLStringFilter;
import org.pr0.straylight.fw.util.StringFilterException;
import org.pr0.straylight.fw.util.StringUtils;
import org.pr0.straylight.fw.util.ArrayUtils;
import org.pr0.straylight.fw.servlet.RequestHandler;
import org.pr0.straylight.fw.um.User;
import org.pr0.straylight.fs.Forum;
import org.pr0.straylight.fs.context.ContextForum;
import org.pr0.straylight.fs.context.ContextMessage;
import org.pr0.straylight.fs.context.ContextMessageList;
import org.pr0.straylight.fs.context.ContextAuthor;
import org.pr0.straylight.fs.context.ContextDate;

/**
 * This class performs the /forumname/showMessageList request and creates a
 * document context with a list of message according to the request
 * parameter.
 *
 * @author <a href="mailto:ob@pr0.org">Oliver Baltzer</a>
 * @version $Revision: 1.3 $ $Date: 2001/09/27 16:39:50 $
 */
class ShowMessageListRequest implements RequestHandler
{
    /**
     * the default start value if no other provided.
     */
    private static final int DEFAULTSTART = 0;
    
    /**
     * the default count of messages if no other provided.
     */
    private static final int DEFAULTCOUNT = 10;
    
    /**
     * the default sorting if not different provided.
     */
    private static final int DEFAULTSORT = ForumMessageList.BYDATE;

    /**
     * default sort direction.
     */
    private static final boolean DEFAULTREVERSE = true;

    /**
     * default view thread/flat.
     */
    private static final boolean DEFAULTTHREADVIEW = true;

    /**
     * local logger.
     */
    private static Category logger = null;
 
    /**
     * a local reference to the forum object this request is for.
     */
    private Forum forum;
 
    /**
     * the context which will be generated
     */
    private Hashtable context;

    /**
     * the name of the response template.
     */
    private String templatename;

    /**
     * string filter for displaying the subject.
     */
    private static StringFilterChain outgoingFilter = null;
 
    /**
     * handles the request and extracts the parameters from the request and
     * creates the context for the message list.
     *
     * @param request the HTTP request object
     * @param forum the forum object
     */
    protected ShowMessageListRequest(HttpServletRequest request,
                                     Forum forum)
                                     throws ForumServletException
    {
        // XXX DEBUG
        long time = System.currentTimeMillis();
        
        if(logger == null) 
            logger = Category.getInstance(this.getClass().getName());

        // initialize the outgoingFilter if not yet done
        if(outgoingFilter == null)
        {
            outgoingFilter = 
                new StringFilterChain(new TextToHTMLStringFilter());
        }
  
        // make the forum available for other class methods
        this.forum = forum;
        
        // the first message which should be displayed (relative to the
        // forum and sorting)
        String startVal = request.getParameter("start");
        // how many messages should be shown
        String countVal = request.getParameter("count");
        // which criteria should sort the message
        String sortVal = request.getParameter("sort");
        // in which order should be sorted
        String reverseVal = request.getParameter("reverse");
        // list of threads which should be expanded
        String expandVal = request.getParameter("expand");
        // is thread view enabled
        String threadVal = request.getParameter("threadview");
        
        // initialize values with defaults
        boolean reverse = DEFAULTREVERSE;
        int sort = DEFAULTSORT;
        int count = DEFAULTCOUNT;
        int start = DEFAULTSTART;
        boolean threadview = DEFAULTTHREADVIEW;
        
        // should we care about authors
        boolean postrestricted = forum.isPostRestricted();
  
        // the FS BaseURL
        String baseurl = request.getContextPath() 
                       + request.getServletPath();
  
        // the forum BaseURL
        String forumbase = baseurl + "/" + forum.getShortname();
  
        // should be the messages sortet in reverse or normal order
        if(reverseVal != null)
        {
            if(reverseVal.length() == 1 && reverseVal.charAt(0) == '0')
                reverse = false;
            else 
            {
                // set the correct value in parameter automaticly
                logger.warn("[" + request.getRemoteAddr() + "]: "
                            + "Value of 'reverse' parameter invalid.");
            }
        
        }
        
        // do we want thread view enabled
        if(threadVal != null)
        {
            if(threadVal.length() == 1 && threadVal.charAt(0) == '0')
                threadview = false;
            else 
            {
                // set the correct value in parameter automaticly
                logger.warn("[" + request.getRemoteAddr() + "]: "
                            + "Value of 'threadview' parameter invalid.");
            }
        }
  
        // how the messages should be sorted (default by date)
        if(sortVal != null)
        {
            if(sortVal.equals("Date")) 
                sort = ForumMessageList.BYDATE;
            else if(sortVal.equals("Subject")) 
                sort = ForumMessageList.BYSUBJECT;
            // XXX we currently do not suppot sort by author
            else 
            {
                // set the correct value in parameter automaticly
                logger.warn("[" + request.getRemoteAddr() + "]: "
                            + "Value of 'sort' parameter invalid.");
            }
        }
        
        // get the count value
        try
        {
            // take the value of count only if it is a number and greater than 0
            if(countVal != null)
            {
                if((count = Integer.parseInt(countVal)) <= 0) 
                {
                    count=10;
                }
            }
        }
        catch(NumberFormatException e)
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Value of 'count' parameter invalid.");
        }
        
        // get the start value
        try
        {
            // take the value of start only if it is a number and greater
            if(startVal != null)
            {
                if((start = Integer.parseInt(startVal)) < 0) 
                    start = 0;
            }
        }
        catch(NumberFormatException e)
        {
            logger.warn("[" + request.getRemoteAddr() + "]: "
                        + "Value of 'start' parameter invalid.");
        }
        
        // the list of message id which should be expanded
        int toexpand[];
        
        // the expanded threads
        if(expandVal != null)
        {
            String nums[] = StringUtils.split(expandVal, ",");
            toexpand = new int[nums.length];
            for(int i = 0; i < nums.length; i++)
            {
                try
                {
                    toexpand[i] = Integer.parseInt(nums[i]);
                }
                catch(NumberFormatException e)
                {
                    logger.warn("[" + request.getRemoteAddr() + "]: "
                                + "Value of 'expand' parameter is "
                                + "invalid. Skipping field.");
                    toexpand[i]=0;
                }
            }
            // remove skipped or invalid fields
            toexpand = ArrayUtils.remove(toexpand, 0);
        }
        else
        {
            toexpand=new int[0];
        }
        Arrays.sort(toexpand);
        
        // XXX DEBUG
        logger.debug("Parameter-Parsing: " 
                     + (System.currentTimeMillis() - time));
        
        // XXX restart timer
        time = System.currentTimeMillis();
  
        // the generated list of ForumMessages
        Vector list = null;
        
        DBConnection dbcon = null; 
        try
        {   
            // get a connection from the pool
            dbcon = ForumServlet.dbpool.getConnection();
            if(! threadview) 
            {
                // create a flat list
                list=new ForumMessageList(forum, start, count + 1, 
                                          sort, reverse, dbcon,
                                          ForumServlet.um);
            }
            else
            {
                // thread view enables get thread list
                list=new ForumMessageList(forum, start, count + 1, sort,
                                          reverse, toexpand, dbcon,
                                          ForumServlet.um);
            }    
            ForumServlet.dbpool.releaseConnection(dbcon);
            dbcon = null;
        }
        catch(ForumMessageListException e)
        {
            if(dbcon != null)
            {
                ForumServlet.dbpool.releaseConnection(dbcon);
                dbcon = null;
            }
            logger.error("[" + request.getRemoteAddr() + "]: "
                        + e.getMessage());
            
            throw new ForumServletException("Cannot create message list",
                                            e.getMessage());
        }
        catch(DBConnectionPoolException e)
        {
            logger.error("[" + request.getRemoteAddr() + "]: "
                         + "Cannot get free database connectioni; "
                         + e.getMessage());
            throw new ForumServletException(
                "Cannot get database connection",
                "Cannot get database connection from database "
                + "connection pool"
            );
        }
  
        // XXX DEBUG
        logger.debug("Database: " + (System.currentTimeMillis() - time));
        // XXX reset timer
        time = System.currentTimeMillis();
        
        // define the ContextMessageList object which contains the
        // context representation of a message list
        ContextMessageList contextmessagelist = new ContextMessageList(); 
  
        // the expected number of message is count + 1 because we are
        // fetching one more message from database to determine if there
        // are enough messages for a next page
        int expectedcount = count + 1;
        // number of shown messages/threads
        int numOnLevelZero = 0;
        // a counter
        int i = 0;
        // the length of the list
        int listlength = list.size();
        // breake flag
        boolean done = false;
        
        // the backjump URL for the show message request, encode the
        // parameter separator
        String back = "&back=" 
                    + makeURL(forumbase, start, count, sort, reverse,
                              threadview, toexpand).replace('&', '$');
        
        // create the Message creation date from timestamp
        while(i < listlength && ! done)
        {
            ForumMessage message = (ForumMessage)list.get(i);
   
            // count messages in top level
            if(message.getReplyLevel() == 0) 
                numOnLevelZero++;
   
            // if number of top level messages equals count
            // stop here and do not process anymore message
            // this is needed to skip the messages which are fetched from the
            // database but not belonging to the current view
            if(numOnLevelZero == expectedcount)
            {
                // because of performance reasons I'll use the unclean
                // break here
                break;
                // done=true;
            }
            else
            {
                int messageid = message.getId();
                User author = message.getAuthor();
                ContextAuthor contextauthor;
                if(author != null)
                {
                    // is her/his eMail address public
                    if(author.isPublicEMail())
                    {   
                        // FIXME: the author's name is not secure in the
                        //        sense of special characters ->
                        //        extend string filter mechanism
                                
                        // put the address in the public context too
                        contextauthor = new ContextAuthor(
                            author.getSurname(),
                            author.getFirstname(),
                            author.getEMail()
                        );
                    }
                    else
                    {
                        // don't put the authors eMail-address in
                        contextauthor = new ContextAuthor(
                            author.getSurname(),
                            author.getFirstname(),
                            ""
                        );
                    }
                }
                else
                {
                    // if the forum is not postrestricted do not look for
                    // authors
                    contextauthor = new ContextAuthor("", "", "");
                }
    
                // get the raw message subject
                String subject = message.getSubject();
                try
                {
                    // expand subject with HTML tags
                    subject = outgoingFilter.filter(subject);
                }
                catch(StringFilterException e)
                {
                    logger.warn("[" + request.getRemoteAddr()
                                + "]: String getted from database not "
                                + "correct. Exception: "
                                + e.getMessage());
                }
                // construct the URL where the message can be accessed
                String accessurl = forumbase 
                                 + "/showMessage?message="
                                 + messageid + back;
                
                SimpleDate date = message.getDate();
                // create the new date context
                
                ContextDate contextdate=new ContextDate(
                    date.getDay(),
                    date.getMonth(),
                    date.getYear(),
                    date.getHour(),
                    date.getMin(),
                    date.getSec()
                );
    
                String toggleExpand = "";
                boolean expanded = false;
                
                if(message.hasReplies())
                {
                    int tmptoexpand[];
                    if(Arrays.binarySearch(toexpand, messageid) >= 0)
                    {
                        tmptoexpand = 
                            ArrayUtils.remove(toexpand, messageid);
                    
                        expanded = true;
                    }
                    else
                    {
                        tmptoexpand =
                            ArrayUtils.add(toexpand, messageid);
                        
                        expanded = false;
                    }
                    
                    toggleExpand = makeURL(forumbase, start, count, sort,
                                           reverse, threadview, 
                                           tmptoexpand);
                }
                
                // add the message to the message list
                contextmessagelist.add(
                    new ContextMessage(
                        contextauthor,
                        subject,
                        null,
                        accessurl,
                        null,
                        new Integer(message.getReplyLevel()),
                        message.hasReplies(),
                        contextdate,
                        expanded,
                        toggleExpand
                    )
                );
                
                // increase the message counter
                i++;
            }
        }
  
        // XXX DEBUG
        logger.debug("Context List: " 
                     + (System.currentTimeMillis() - time));
        // reset the timer
        time = System.currentTimeMillis();
  
        // how many message were retrieved
        int retrieved = numOnLevelZero;
  
        // create links to next pages
        String nextpageurl;
        int startnext = -1;
        int endnext = -1;
        // if we got as much as expected than we need one more page
        if(retrieved == expectedcount)
        {
            // the URL for the next page
            nextpageurl = makeURL(
                forumbase,
                (start + (retrieved - 1)),
                count,
                sort,
                reverse,
                threadview,
                toexpand
            );
            
            startnext = start + retrieved;
            endnext = (start + retrieved + count) - 1;
            // if the number of retrieved messages equals the expected
            // number than this is one more than really shown, so decrease
            // it 
            retrieved--;
        }
        else
        {
            nextpageurl = makeURL(
                forumbase,
                start,
                count,
                sort,
                reverse,
                threadview,
                toexpand
            );
        }
  
        // link to previous page
        String prevpageurl;
  
        int startprev = -1;
        int endprev = -1;
        // only if there could be previous page
        if(start != 0)
        {
            if((start - count) >= 0)
            {
                // if the previous page not the first page
                prevpageurl = makeURL(
                    forumbase,
                    start - count,
                    count,
                    sort,
                    reverse,
                    threadview,
                    toexpand
                );
                
                // we are counting from 1
                startprev = (start - count) + 1; 
                endprev = start;
            }
            else
            {
                // if the previous page the first page
                prevpageurl = makeURL(
                    forumbase,
                    0,
                    count,
                    sort,
                    reverse,
                    threadview,
                    toexpand
                );
                startprev = 1;
                endprev = count;
            }
        }
        else
        {
            prevpageurl = makeURL(
                forumbase,
                0,
                count,
                sort,
                reverse,
                threadview,
                toexpand
            );  
        }
        
        // create the context forum
        ContextForum contextforum = new ContextForum(
            forum.getTitle(),
            forum.getDescription(),
            forum.getHomepage(),
            forumbase,
            null
        );

        // creating the context
        context=new Hashtable();
        // add all public context variables
        context.put("BaseURL", baseurl);
        context.put("Forum", contextforum);
        context.put("StartNum", new Integer(start + 1));
        context.put("EndNum", new Integer(start + retrieved));
        context.put("NextURL", nextpageurl);
        context.put("PrevURL", prevpageurl);
        context.put("StartNext", new Integer(startnext));
        context.put("EndNext", new Integer(endnext));
        context.put("StartPrev", new Integer(startprev));
        context.put("EndPrev", new Integer(endprev));
        context.put("MessageList",contextmessagelist);
        context.put("IsThreadView", new Integer(threadview ? 1 : 0));
        context.put("ToggleThreadView", 
                    makeURL(forumbase, 0, count, sort, reverse,
                    !threadview, toexpand));
        context.put("SortByDate", 
                    makeURL(forumbase, 0, count, ForumMessageList.BYDATE,
                    sort == ForumMessageList.BYDATE ? !reverse : reverse, 
                    threadview, toexpand));
        context.put("SortBySubject", 
                    makeURL(forumbase, 0, count, 
                    ForumMessageList.BYSUBJECT,
                    sort == ForumMessageList.BYSUBJECT 
                        ? !reverse : reverse, 
                    threadview, toexpand));
        // assign template name
        templatename="ShowMessageList";

        logger.debug("Make Context: " 
                     + (System.currentTimeMillis() - time));
    }
 
    /**
     * generates a URL to this function with the given parameter.
     *
     * @param forumbase the base URL for the current forum
     * @param start the value for the start argument
     * @param count the value for the count argument
     * @param reverse the value for the reverse argument
     * @param sort the value for the sort argument
     * @param thread the value for the threadview argument
     * @param toexpand an array of message ids
     */
    private String makeURL(String forumbase, int start, int count, 
                           int sort, boolean reverse, boolean threadview,
                           int toexpand[])
    {
        String retval = forumbase + "/showMessageList?start=" + start;
        if(count != DEFAULTCOUNT) 
            retval += "&count=" + count;
        
        if(reverse != DEFAULTREVERSE) 
            retval += "&reverse=" + (reverse ? "1" : "0");
        
        if(sort != DEFAULTSORT)
        { 
            if(sort == ForumMessageList.BYDATE) 
                retval += "&sort=Date";
            else if(sort == ForumMessageList.BYSUBJECT) 
                retval += "&sort=Subject";
        }
        
        if(threadview != DEFAULTTHREADVIEW) 
            retval += "&threadview=" + (threadview ? "1" : "0");
        
        if(toexpand.length != 0)
        {
            String nums[] = new String[toexpand.length];
            
            for(int j = 0; j < toexpand.length; j++) 
                nums[j] = Integer.toString(toexpand[j]);
            
            retval += "&expand=" + StringUtils.join(nums, ",");
        }
        
        return retval;
    }
  
    public Hashtable getContext()
    {
        return context;
    }

    public String getTemplateName()
    {
        return templatename;
    }
}
