## $Id: newreply.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - create new reply")
#set($Document.Author="Forum System Servlet")

<h1>Reply to message: ${Message.Subject}</h1>
#if(${Message.Author})
<p>
 posted by ${Message.Author.Fullname} on 
 ${Message.PostingDate.Year}-${Message.PostingDate.Month}-${Message.PostingDate.Day}
</p>
#end
#if(${Error}!=0)
<p>
 <strong class="error">
 #if(${Error}==2)
 You must provide a subject.
 #end
 #if(${Error}==1)
 The message must have a content.
 #end
 #if(${Error}==3)
 HTML is not allowed in the message.
 #end
 </strong>
</p>
#end
<form action="${Forum.Base}/newReply" method="POST">
 <table border="0" cellspacing="0" cellpadding="5">
  <tr>
   <th valign="top" align="right">
    Subject:
   </th>
   <td valign="top" align="left">
    <input 
     type="text" name="subject" size="60" maxlength="150"
     value="${Message.Subject}"
    >
   </td>
  </tr>
  <tr>
   <th valign="top" align="right">
    Message:
   </th>
   <td valign="top" align="left">
    <textarea 
     name="body" rows="15" cols="70" wrap="virtual"
    >
#if(${Message.Author})
On ${Message.PostingDate.Day}.${Message.PostingDate.Month}.${Message.PostingDate.Year} ${Message.Author.Fullname} wrote:
#end
${Message.Body}</textarea>
   </td>
  </tr>
  <tr>
   <td valign="top" align="right">
    &nbsp;
   </td>
   <td valign="top" align="left">
    <input type="hidden" name="message" value="${MessageId}">
    <input type="hidden" name="back" value="${BackEnc}">
    <input type="submit" name="submit" value="Send Reply">&nbsp;&nbsp;
    <input type="reset" value="Reset">
   </td>
  </tr>
  <tr>
   <td></td>
   <td valign="top" align="left">
    <a href="${BackLink}"><strong class="darklink">Back to message 
    list</strong></a>
   </td>
  </tr>
 </table>
</form>
