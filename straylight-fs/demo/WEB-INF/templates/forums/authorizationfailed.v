$Document.setTitle("401 Authorization failed")

<h1>Authorization required</h1>
<p>
 You have requested a <strong> 
 #if( $RestrictionType == 0 )
  READ
 #else
  POST 
 #end
 </strong>
 access for forum '${Forum.Title}' but you do not have permissions.
</p>
<p>
 #if( $RegistrationAllowed == 1 )
  #if( $UserID )
   You are a registered user and all you need is to enable your account
   #if( $RestrictionType == 0 )
    <a href="${Forum.Base}/registerUser?type=read&enable=1&uid=${UserID}&back=${BackLink}"><strong class="darklink">here</strong></a>. 
   #else
    <a href="${Forum.Base}/registerUser?type=post&enable=1&uid=${UserID}&back=${BackLink}"><strong class="darklink">here</strong></a>. 
   #end
  #else
   Probably you are not a registered user. You can register
   #if( $RestrictionType == 0 )
    <a href="${Forum.Base}/registerUser?type=read&back=${BackLink}"><strong class="darklink">here</strong></a>. 
   #else
    <a href="${Forum.Base}/registerUser?type=post&back=${BackLink}"><strong class="darklink">here</strong></a>. 
   #end
  #end
 #end
</p>
<p>
 #if( $RestrictionType == 0 )
  <a href="${Base}/"><strong class="darklink">Back to the forum 
  list</strong></a>  
 #else
  <a href="${Forum.Base}/"><strong class="darklink">Back to message 
  list</strong></a>  
 #end
</p>
<p>
