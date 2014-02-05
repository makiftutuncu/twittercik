package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current
import play.api.Logger
import helpers.SHA512Generator

case class Session(cookieid: String, username: String)

object Session
{
  val session = {
    get[String]("cookieid") ~ get[String]("username") map {
      case cookieid ~ username => Session(cookieid, username)
    }
  }

  def create(username: String): Option[String] = {
    User.read(username) match {
      case Some(user: User) =>
        val cookieid: String = SHA512Generator.generate(username + System.currentTimeMillis())
        DB.withConnection { implicit c =>
          SQL("insert into sessions (cookieid, username) values ({cookieid}, {username})")
            .on('cookieid -> cookieid, 'username -> username).executeUpdate()
        }
        Option(cookieid)
      case _ =>
        Logger.info(s"Session.create() - Cannot create a session for user that doesn't exist with name $username!")
        None
    }
  }

  def read(cookieid: String): Option[Session] = {
    DB.withConnection { implicit c =>
      val sessions: List[Session] = SQL("select * from sessions where cookieid={cookieid} limit 1")
        .on('cookieid -> cookieid).as(session *)

      val result = sessions.headOption
      result match {
        case Some(_) => Logger.debug(s"Session.read() - Session with cookieid $cookieid is found, returning it...")
        case _ => Logger.info(s"Session.read() - Session with cookieid $cookieid is not found!")
      }
      result
    }
  }

  def delete(cookieid: String): Boolean = {
    DB.withConnection { implicit c =>
      val result = SQL("delete from sessions where cookieid = {cookieid}").on('cookieid -> cookieid).executeUpdate()
      (result > 0)
    }
  }
}