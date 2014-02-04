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

  def getByCookieId(cookieid: String): Option[Session] = {
    DB.withConnection { implicit c =>
      val sessions: List[Session] = SQL("select * from sessions where cookieid={cookieid} limit 1").on('cookieid -> cookieid).as(session *)

      if(sessions.length == 1)
      {
        Logger.debug(s"Session.getByCookieId() - Session with cookieid $cookieid is found, returning it...")
        sessions.headOption
      }
      else
      {
        Logger.info(s"Session.getByCookieId() - Session with cookieid $cookieid is not found!")
        None
      }
    }
  }

  def create(username: String): String = {
    User.getByUsername(username) match {
      case Some(user: User) =>
        val cookieid: String = SHA512Generator.generate(username + System.currentTimeMillis())
        DB.withConnection { implicit c =>
          SQL("insert into sessions (cookieid, username) values ({cookieid}, {username})").on('cookieid -> cookieid, 'username -> username).executeUpdate()
        }
        cookieid

      case _ =>
        Logger.error(s"Session.create() - Cannot create a session for user that doesn't exist with name $username!")
        throw new IllegalArgumentException()
    }
  }

  def delete(cookieid: String) = {
    DB.withConnection { implicit c =>
      SQL("delete from sessions where cookieid = {cookieid}").on('cookieid -> cookieid).executeUpdate()
    }
  }
}