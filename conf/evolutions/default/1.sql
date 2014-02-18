# Twittercık schema

# --- !Ups
CREATE TABLE users (
    username VARCHAR(32) NOT NULL PRIMARY KEY,
    password CHAR(128) NOT NULL,
    salt CHAR(128) NOT NULL,
    fbuserid VARCHAR(32)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE facebookusers (
    userid VARCHAR(32) NOT NULL PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    accesstoken VARCHAR(256) NOT NULL,
    logintime BIGINT NOT NULL,
    expire BIGINT NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE tweetciks (
    id INT(10) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(24) NOT NULL,
    content VARCHAR(140) NOT NULL,
    tweetcikdate BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sessions (
    cookieid CHAR(128) NOT NULL UNIQUE,
    username VARCHAR(24) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs
DROP TABLE users;
DROP TABLE facebookusers;
DROP TABLE tweetciks;
DROP TABLE sessions;