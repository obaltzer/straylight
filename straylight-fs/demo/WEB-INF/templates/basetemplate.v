<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
 <head>

  <title>
   ${Document.Title}
  </title>
  
  <!-- Metadaten angeben -->
  <meta name="description" content="${Document.Description}">
  <meta name="author" content="${Document.Author}">
  <meta name="keywords" content="${Document.Keywords}">
  <meta name="date" content="${Document.Date}">
  <meta http-equiv="pragma" content="no-cache"> 

  <style type="text/css">
   BODY,A,P,TD,TH,LI
   {
    font-family: Lucida,Helvetica,Verdana,Arial;
    font-size: 10pt;
    color: #005760;
   }
   
   A
   {
    text-decoration: underline;
   }
   
   STRONG.error
   {
    color: #ff0000;
    fond-weight: bild;
   }

   STRONG.light
   {
    color: #6ab9c1;
    font-weight: bold;
   }
   
   STRONG.lightlink
   {
    color: #6ab9c1;
    font-weight: bold;
    text-decoration: underline;
   }
   
   STRONG.dark
   {
    color: #005760;
    font-weight: bold;
   }
    
   STRONG.darklink
   {
    color: #005760;
    font-weight: bold;
    text-decoration: underline;
   }
    
   BIG
   {
    font-size: 18pt;
   }
 
   H1
   {
    font-size: 14pt;
   }

  </style>
 </head>
 <body bgcolor="#000000" link="#0000FF" alink="#FF0000" vlink="#6666FF">
  <table width="100%" cellspacing="0" cellpadding="1" border="0">
   <tr>
    <td bgcolor="#bababa" align="left" valign="top">
     <table width="100%" cellspacing="0" cellpadding="10" border="0">
      <tr>
       <td bgcolor="#005760">
        <font color="#6ab9c1" align="left" valign="middle">
         <big>Straylight Forum Test</big>
        </font>
       </td>
      </tr>
      <tr>
       <td bgcolor="#6ab9c1" align="left" valign="top">
        ${Document.Body}
       </td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </body>
</html>
