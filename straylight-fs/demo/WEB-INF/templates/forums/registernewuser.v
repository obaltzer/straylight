## $Id: registernewuser.v,v 1.1 2002/02/10 18:54:19 racon Exp $

#set($Document.Title="${Forum.Title} - Register a new user")
#set($Document.Author="Forum System Servlet")

<h1>Register a new user</h1>
#if(${error} == "UsernameExists")
<p>
 <font color="#ff0000">There is already an existing user with the
 username '$!{Username}'. Please choose another one.</font>
</p>
#end
<form action="${Forum.Base}/registerUser" method="GET">
 <table border="0" cellspacing="0" cellpadding="5">
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Username" || ${error} == "UsernameExists")
     <font color="#ff0000">Username:</font>
    #else
     Username:
    #end
   </th>
   <td valign="middle" align="left">
    <input 
     type="text" name="username" size="15" maxlength="15"
     value="$!{Username}"
    >
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
     type="password" name="password" value="$!{Password}" 
     size="15" maxlength="15">
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Firstname")
     <font color="#ff0000">Firstname:</font>
    #else
     Firstname:
    #end
   </th>
   <td valign="middle" align="left">
    <input 
     type="text" name="firstname" size="20" maxlength="60"
     value="$!{Firstname}"
    >
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Surname")
     <font color="#ff0000">Surname:</font>
    #else
     Surname:
    #end
   </th>
   <td valign="middle" align="left">
    <input 
     type="text" name="surname" size="20" maxlength="60"
     value="$!{Surname}"
    >
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Gender")
     <font color="#ff0000">Gender:</font>
    #else
     Gender:
    #end
   </th>
   <td valign="middle" align="left">
    <input type="radio" name="gender" 
     #if(${Gender} == "f")
      checked
     #end
     value="f"> Female
    &nbsp;&nbsp;
    <input type="radio" name="gender" 
     #if(${Gender} == "m")
      checked
     #end
     value="m"> Male
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "EMail")
     <font color="#ff0000">E-Mail address:</font>
    #else
     E-Mail address:
    #end
   </th>
   <td valign="middle" align="left">
    <input 
     type="text" name="email" size="20" maxlength="60"
     value="$!{EMail}"
    >
    <input type="checkbox" name="publicemail" 
     #if(${publicEMail} == "y")
      checked
     #end
     value="y"> public 
   </td>
  </tr>
  <tr>
   <th valign="middle" align="right">
    #if(${error} == "Homepage")
     <font color="#ff0000">Homepage address *:</font>
    #else
     Homepage address <font color="#ff0000">*</font>:
    #end 
   </th>
   <td valign="middle" align="left">
    <input 
     type="text" name="homepage" size="40" maxlength="120"
     value="$!{Homepage}"
    >
    <input type="checkbox" name="publichomepage" 
     #if(${publicHomepage} == "y")
      checked
     #end
     value="y"> public 
   </td>
  </tr>
  <tr>
   <td valign="middle" align="right">
    <input type="hidden" name="type" value="${Type}">
  <!--  <input type="hidden" name="back" value="$!{BackLink}"> -->
    <input type="submit" name="submit" value="Send">
   </td>
   <td valign="middle" align="left">
    <input type="reset" value="Reset">
   </td>
  </tr>
 </table>
</form>
