## $Id: showmessage.v,v 1.1 2002/02/10 18:54:19 racon Exp $
#set( $PicsBase = "../../images" )
<h1>Message: ${Message.Subject}</h1>
<p>
 posted by ${Message.Author.Fullname} 
 on 
 ${Message.PostingDate.Year}-${Message.PostingDate.Month}-${Message.PostingDate.Day}:
</p>
<table border="0">
 <tr>
  <td>
   &nbsp;&nbsp;&nbsp;
  </td>
  <td valign="top" align="left" bgcolor="#a7d0d3">
   <tt>
    ${Message.Body}
   </tt>
  </td>
 </tr>
</table>
<p>
<a href="${Message.ReplyURL}"><strong class="darklink">Reply to this 
 message</strong></a>
&nbsp;&nbsp;&nbsp;
<a href="${BackLink}"><strong class="darklink">Back to Message
 List</strong></a>
</p>
<hr>
#if(${ThreadList})
<h2>Thread</h2>
<p>
 <a href="${ThreadView}"><strong class="darklink">Turn off Thread 
  View</strong></a>
</p>
<table width="750" border="0" cellspacing="2" cellpadding="4">
 <tr>
  <td align="left" valign="top" width="50%" bgcolor="#005760">
   <strong class="light">Subject</strong>
  </td>
  <td align="left" valign="top" width="25%" bgcolor="#005760">
   <strong class="light">Author</strong>
  </td>
  <td align="left" valign="top" width="25%" bgcolor="#005760">
   <strong class="light">Posting Date</strong>
  </td>
 </tr>
 #foreach($Message in ${ThreadList})
 #if($Color == "#6ab9c1")
  #set( $Color = "#a7d0d3" )
 #else
  #set( $Color = "#6ab9c1" )
 #end
 <tr bgcolor="${Color}">
  <td align="left" valign="top">
   #foreach($a in [0 .. ${Message.ReplyLevel}])
    <img src="${PicsBase}/empty.gif" height="9" width="9"
     alt="&nbsp;">
   #end
   <img src="${PicsBase}/empty.gif" height="9" width="9"
    alt="&nbsp;">
   <a href="${Message.AccessURL}">${Message.Subject}</a>
  </td>
  <td align="left" valign="top">
   ${Message.Author.Fullname}
  </td>
  <td align="left" valign="top">
   ${Message.PostingDate.Year}-${Message.PostingDate.Month}-${Message.PostingDate.Day}
  </td>
 </tr>
 #end
</table>
#else
<p>
 <a href="${ThreadView}"><strong class="darklink">Turn on Thread
  View</strong></a>
</p>
#end

