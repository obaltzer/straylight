## $Id: newreplyok.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - new reply was created")
#set($Document.Author="Forum Application Servlet")

<h1>Reply created</h1>
<p>
 You reply with subject '<strong>${Message.Subject}</strong>' was 
 sucessfully created in forum '${Forum.Title}'.
</p>
<p>
 <a href="${BackLink}"><strong class="darklink">Back to message list</strong></a>
</p>
