/* $Id: DBUserManager.sql,v 1.1 2002/02/10 18:52:43 racon Exp $ */

/*
 * Sets up database tables needed by the DBUserManager UserManager
 * implementation from the Straylight Framework.
 */

/*
 *  The Table containing the users.
 */

DROP TABLE IF EXISTS User;

CREATE TABLE User (
    /* the Id of the user */
    Id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    /* the short username of the user */
    Username TEXT NOT NULL,
    /* the not encrypted password */
    Password TEXT NOT NULL,
    /* the real surname of the user */
    Surname TEXT NOT NULL,
    /* the real firstname of the user */
    Firstname TEXT NOT NULL,
    /* the gender 0 - male 1 - female */
    Gender INTEGER UNSIGNED NOT NULL,
    /* the email address of the user */
    EMail TEXT NOT NULL,
    /* an optional homepage URL */
    Homepage TEXT,
    /* 1 if the eMail address is public available */
    PublicEMail INTEGER UNSIGNED NOT NULL,
    /* 1 if the homepage URL is public available */
    PublicHomepage INTEGER UNSIGNED NOT NULL,
    /* is set to 1 if the user is marked as deleted */
    Deleted INTEGER UNSIGNEd NOT NULL DEFAULT 1,
    /* timestamp where this entry was modified the last time */
    LastModified TIMESTAMP(14) NOT NULL,
    /* timestamp when this entry was created */
    CreateDate TIMESTAMP(14) NOT NULL
);

INSERT INTO User (
    Username,
    Surname,
    Firstname,
    EMail,
    Password,
    Homepage,
    Gender,
    PublicEMail,
    PublicHomepage,
    CreateDate
) VALUES (
    'testuser',
    'Straylight',
    'Testuser',
    'test@test.com',
    'password',
    'http://straylight.sf.net',
    0,
    0,
    0,
    CURRENT_TIMESTAMP
);
