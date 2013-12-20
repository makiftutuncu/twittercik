# Twittercık schema

# --- !Ups

CREATE TABLE users (
    username varchar(24) NOT NULL,
    password varchar(32) NOT NULL
);

CREATE SEQUENCE tweetcik_id_seq;
CREATE TABLE tweetciks (
    id integer NOT NULL DEFAULT nextval('tweetcik_id_seq'),
    username varchar(24) NOT NULL,
    content varchar(140) NOT NULL,
    tweetcikdate bigint NOT NULL
);

CREATE TABLE sessions (
    uuid varchar(36) NOT NULL,
    username varchar(24) NOT NULL
);

# --- !Downs

DROP TABLE users;
DROP TABLE tweetciks;
DROP TABLE sessions;
DROP SEQUENCE tweetcik_id_seq;