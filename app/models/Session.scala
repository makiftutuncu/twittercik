package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current
import play.api.Logger
import helpers.SHA512Generator

/**
 * A model for keeping a user session
 *
 * @param cookieid  A generated id kept in cookie identifying the user
 * @param username  Name of the user whose session is identified by cookieid
 */
case class Session(cookieid: String, username: String)

/**
 * Companion object of Session acting as data access layer
 */
object Session
{
  /**
   * A result set parser for session records in database, maps records to a Session object
   */
  val session = {
    get[String]("cookieid") ~ get[String]("username") map {
      case cookieid ~ username => Session(cookieid, username)
    }
  }

  /**
   * Creates a session for given user in the database
   *
   * @param username  Name of the user
   *
   * @return  An optional Session if successful
   */
  def create(username: String): Option[Session] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(User.user *) match {
          case users: List[User] =>
            if(users.isEmpty) {
              Logger.error(s"Session.create() - Cannot create a session for user that doesn't exist with name $username!")
              None
            }
            else {
              val cookieid: String = SHA512Generator.generate(username + System.currentTimeMillis())
              SQL("insert into sessions (cookieid, username) values ({cookieid}, {username})")
                .on('cookieid -> cookieid, 'username -> username).executeUpdate()
              val sessions: List[Session] = SQL("select * from sessions where cookieid={cookieid} limit 1")
                .on('cookieid -> cookieid).as(session *)
              sessions.headOption
            }
          case _ =>
            Logger.error(s"Session.create() - Cannot create a session for user that doesn't exist with name $username!")
            None
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Session.create() - ${e.getMessage}")
        None
    }
  }

  /**
   * Reads a session with given cookie id from the database
   *
   * @param cookieid  Cookie id of the session
   *
   * @return  An optional Session if successful
   */
  def read(cookieid: String): Option[Session] = {
    try {
      DB.withConnection { implicit c =>
        val sessions: List[Session] = SQL("select * from sessions where cookieid={cookieid} limit 1")
          .on('cookieid -> cookieid).as(session *)
        sessions.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Session.read() - ${e.getMessage}")
        None
    }
  }

  /**
   * Deletes a session with given cookie id from the database
   *
   * @param cookieid  Cookie id of the session
   *
   * @return  true if successful, false otherwise
   */
  def delete(cookieid: String): Boolean = {
    try {
      DB.withConnection { implicit c =>
        val result = SQL("delete from sessions where cookieid = {cookieid}")
          .on('cookieid -> cookieid).executeUpdate()
        result > 0
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Session.delete() - ${e.getMessage}")
        false
    }
  }
}