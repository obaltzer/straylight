## $Id: messagelist.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($PicsBase = "../../images" )

<h1>Forum: ${Forum.Title}</h1>
<p>
 ${Forum.Description}
</p>
<table width="750" border="0" cellspacing="2" cellpadding="4">
 <tr>
  <td colspan="4">
   <strong class="dark">
    #if(${IsThreadView} == 1)
     Threads 
    #else
     Messages
    #end
    ${StartNum} to ${EndNum}</strong>
  </td>
 </tr>
 <tr>
  <td align="left" valign="top" width="50%" bgcolor="#005760">
   <a href="${SortBySubject}"><strong class="lightlink">Subject</strong></a>
  </td>
  <td align="left" valign="top" width="25%" bgcolor="#005760">
   <strong class="light">Author</strong>
  </td>
  <td align="left" valign="top" width="25%" bgcolor="#005760">
   <a href="${SortByDate}"><strong class="lightlink">Posting Date</strong></a>
  </td>
 </tr>
 #foreach($Message in ${MessageList})
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
#if(${Message.HasReply}==1)
 #if(${Message.IsExpanded}==1)
  <a href="${Message.ToggleExpandURL}"><img src="${PicsBase}/minus.gif" height="9" width="9" alt="-" border="0"></a>
 #else
  <a href="${Message.ToggleExpandURL}"><img src="${PicsBase}/plus.gif" height="9" width="9" alt="+" border="0"></a>
 #end
#else
<img src="${PicsBase}/empty.gif" height="9" width="9"
 alt="&nbsp;">
#end
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
 <tr>
  <td colspan="4" align="left" valign="top" bgcolor="#005760">
   &nbsp;&nbsp;&nbsp;
   #if(${StartPrev} != -1)
    <a href="${PrevURL}"><strong class="light">&lt;&lt;&lt; 
    [ ${StartPrev}-${EndPrev} ]</strong></a>
   #end
   #if($StartNext != -1 && $StartPrev != -1)
    &nbsp;&nbsp;&nbsp;
   #end
   #if(${StartNext} != -1)
    <a href="${NextURL}"><strong class="light">[ ${StartNext}-${EndNext} ] 
     &gt;&gt;&gt;</strong></a>
   #end
  </td>
 </tr>
 <tr>
  <td align="left" valign="top">
   <a href="${Forum.Base}/newThread"><strong class="darklink">New 
    Thread</strong></a>
   &nbsp;&nbsp;&nbsp;
   #if($IsThreadView == 1)
    <a href="${ToggleThreadView}"><strong class="darklink">Turn off Thread 
     View</strong></a>
   #else
    <a href="${ToggleThreadView}"><strong class="darklink">Turn on Thread 
     View</strong></a>
   #end
  </td>
 </tr>
</table>

