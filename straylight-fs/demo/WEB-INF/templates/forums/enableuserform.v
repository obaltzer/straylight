## $Id: enableuserform.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - Enable access for an existing user")
#set($Document.Author="Forum System Servlet")

<h1>Enable access for an existing user</h1>
<p>
 You are an registered user and you are requesting 
#if(${Type} == "post")
 <strong>POST</strong>
#else
 <strong>READ</strong>
#end
 access to the forum ${Forum.Title}.<br>
 To enable your account for this access please enter the
 password for user <strong>${Username}</strong> below.
</p>
#if(${error} == "Password")
<p>
 <strong class="error">Invalid password for user ${Username}.</strong>
</p>
#end
<form action="${Forum.Base}/registerUser" method="GET">
 <table border="0" cellspacing="0" cellpadding="5">
  <tr>
   <th valign="middle" align="right">
     Username:
   </th>
   <td valign="middle" align="left">
    <strong>${Username}</strong>
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Password")
     <font color="#ff0000">Password:</font>
    #else
     Password:
    #end
   </th>
   <td valign="middle" align="left">
    <input 
     type="password" name="password" value="" 
     size="15" maxlength="15">
   </td>
  </tr>
  <tr>
   <td valign="middle" align="right">
    <input type="hidden" name="type" value="${Type}">
    <input type="hidden" name="uid" value="${UserId}">
    <input type="submit" name="submit" value="Submit">
   </td>
   <td valign="middle" align="left">
    <input type="reset" value="Reset">
   </td>
  </tr>
 </table>
</form>
