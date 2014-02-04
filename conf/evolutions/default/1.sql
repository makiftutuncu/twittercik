# Twittercık schema

# --- !Ups
CREATE TABLE users (
    id INT(10) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(24) NOT NULL UNIQUE,
    password CHAR(128) NOT NULL,
    salt CHAR(128) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE tweetciks (
    id INT(10) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(24) NOT NULL,
    content VARCHAR(140) NOT NULL,
    tweetcikdate BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE sessions (
    cookieid CHAR(128) NOT NULL UNIQUE,
    username VARCHAR(24) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs
DROP TABLE users;
DROP TABLE tweetciks;
DROP TABLE sessions;