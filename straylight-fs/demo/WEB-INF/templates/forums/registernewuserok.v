## $Id: registernewuserok.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - New user has been registered")
#set($Document.Author="Forum System Servlet")

<h1>New user has been registered</h1>
<p>
 The new user with username <strong>${Username}</strong> was 
 created on the system and has now  
 <strong>
 #if(${Type} == "post")
  POST
 #else
  READ
 #end
 </strong>
 access on forum ${Forum.Title}.
</p>
<p>
 <a href="${Forum.Base}/"><strong class="darklink">Back to message
 list</strong></a>
</p>
