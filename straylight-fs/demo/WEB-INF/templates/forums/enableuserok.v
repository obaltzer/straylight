## $Id: enableuserok.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - Enabling User Successful")
#set($Document.Author="Forum System Servlet")

<h1>Enabled User Successfully</h1>
<p>
 User <strong>${Username}</strong> has been successfully enabled for
#if(${Type} == "post")
 <strong>POST</strong>
#else
 <strong>READ</strong>
#end
 access to the forum ${Forum.Title}. 
<p>
 <a href="${Forum.Base}/"><strong class="darklink">Back to the message
 list</strong></a> 
</p>
