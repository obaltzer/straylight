## $Id: newthreadok.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - new thread created")
#set($Document.Author="Forum Application Servlet")

<h1>New thread was created in ${Forum.Title}</h1>
<p>
 Your new thread with title '<strong>${Message.Subject}</strong>' 
 was successfully created in '${Forum.Title}'.
</p>
<p>
 <a href="${BackLink}/"><strong class="darklink">Mack to message
 list</strong></a>
</p>
