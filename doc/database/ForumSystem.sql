/* $Id: ForumSystem.sql,v 1.1 2002/02/10 18:52:43 racon Exp $ */

/*
 * This SQL file is used to setting up the database for the
 * Forum System. It is designed for a MySQL DBMS but with small changes it
 * should also work with other DBMS.
 */

/*
 * The table conatining the forums.
 */

DROP TABLE IF EXISTS Forum;

CREATE TABLE Forum (
    /* The Id if the forum */
    Id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    /* A short title for the forum */
    Title TEXT NOT NULL,
    /* A detailed description of the forum */
    Description TEXT NOT NULL,
    /* the short name for the forum used in the URL */
    Shortname TEXT NOT NULL,
    /* an optional homepage URL */
    Homepage TEXT,
    /* 1 if forum is read restricted */
    ReadRestricted INTEGER UNSIGNED NOT NULL,
    /* 1 if the registration for this forum is allowed */
    ReadRegisterAllowed INTEGER UNSIGNED NOT NULL,
    /* 1 if the registration has to be confirmed by the 
    * administrator */
    ReadRegisterConfirm INTEGER UNSIGNED NOT NULL,
    /* 1 if the forum is post restricted */
    PostRestricted INTEGER UNSIGNED NOT NULL,
    /* 1 if it is allowed to register for post access */
    PostRegisterAllowed INTEGER UNSIGNED NOT NULL,
    /* 1 if the registration must be confirmed */
    PostRegisterConfirm INTEGER UNSIGNED NOT NULL,
    /* the timestamp automatically setted on insert and update */ 
    LastModified TIMESTAMP(14) NOT NULL,
    /* the date of creating this entry */
    CreateDate TIMESTAMP(14) NOT NULL
);

INSERT INTO Forum (
    Title,
    Description,
    Shortname,
    Homepage,
    ReadRestricted,
    ReadRegisterAllowed,
    ReadRegisterConfirm,
    PostRestricted,
    PostRegisterAllowed,
    PostRegisterConfirm,
    CreateDate
) VALUES (
    'Testforum',
    'This is a forum to test the functionality of the Forum System.',
    'testforum',
    'http://straylight.sf.net',
    0,
    0,
    0,
    1,
    0,
    0,
    CURRENT_TIMESTAMP
);
                      
/*
 * The table which maps the users to particular forums.
 */

DROP TABLE IF EXISTS ForumUserMap;

CREATE TABLE ForumUserMap (
    /* the forum Id */ 
    ForumId INTEGER UNSIGNED NOT NULL,
    /* the user Id */
    UserId INTEGER UNSIGNED NOT NULL,
    /* is this user an admin of the forum */
    Admin INTEGER UNSIGNED NOT NULL,
    /* does this user has read permissions in the forum */
    ReadPermit INTEGER UNSIGNED NOT NULL,
    /* does this user has write permissions on the forum */
    PostPermit INTEGER UNSIGNED NOT NULL
);

INSERT INTO ForumUserMap SELECT 
    Forum.Id,
    User.Id,
    0,
    2,
    2 
FROM
    Forum, 
    User 
WHERE 
    Forum.Shortname='testforum' 
    AND
    User.Username='testuser';

/*
 * The message table.
 */
DROP TABLE IF EXISTS ForumMessage;

CREATE TABLE ForumMessage (
    /* the unique message id */
    Id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    /* the subject of the message */
    Subject TEXT NOT NULL,
    /* the body of the message */
    Body TEXT NOT NULL,
    /* the forum Id of the forum the message belongs to */
    ForumId INTEGER UNSIGNED NOT NULL,
    /* the UserId of the author 
     * null - anonymous forum
     * userid - of the author */
    AuthorId INTEGER UNSIGNED,
    /* this message belongs to thread message id 
     * null - this message is a thread root */
    Thread INTEGER UNSIGNED,
    /* this message is a reply to message Id 
     * null - this message is a thread root */
    ReplyTo INTEGER UNSIGNED,
    /* the reply level of this message */
    ReplyLevel INTEGER UNSIGNED NOT NULL,
    /* 1 if this message has at least one reply */
    HasReply INTEGER UNSIGNED NOT NULL,
    /* the IP Address of the host created this message */
    HostAddress VARCHAR(15) NOT NULL,
    /* timestamp when this message was modified the last time */
    LastModified TIMESTAMP(14) NOT NULL,
    /* timestamp when the message was created */
    CreateDate TIMESTAMP(14) NOT NULL
);

INSERT INTO ForumMessage (
    Subject,
    Body,
    ForumId,
    AuthorId,
    Thread,
    ReplyTo,
    ReplyLevel,
    HasReply,
    HostAddress,
    CreateDate
) SELECT 
    'Test Message',
    'This is just a test message to show how it can works.',
    Forum.Id,
    User.Id,
    NULL,
    NULL,
    0,
    0,
    '127.0.0.1',
    CURRENT_TIMESTAMP
FROM
    Forum, 
    User
WHERE  
    Forum.Shortname='testforum' 
    AND 
    User.Username='testuser';
