package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current

case class Session(uuid: String, username: String)

object Session
{
  val session = {
    get[String]("uuid") ~ get[String]("username") map {
      case uuid ~ username => Session(uuid, username)
    }
  }

  def getSession(uuid: String): Option[Session] = {
    DB.withConnection { implicit c =>
      val sessions: List[Session] = SQL("select * from sessions where uuid={uuid}").on(
        'uuid -> uuid
      ).as(session *)

      if(sessions.length != 0)
      {
        println("Session.getSession() - Found session in db with: " + uuid)
        Option(sessions(0))
      }
      else
      {
        println("Session.getSession() - Couldn't find session in db with: " + uuid)
        None
      }
    }
  }

  def create(username: String): String = {
    val uuid: String = generateUUID()
    println("Session.create() - Creating a session in db with: " + uuid + " for username " + username + "...")
    DB.withConnection { implicit c =>
      SQL("insert into sessions (uuid, username) values ({uuid}, {username})").on(
        'uuid -> uuid, 'username -> username
      ).executeUpdate()
    }
    uuid
  }

  def delete(uuid: String) = {
    println("Session.delete() - Deleting the session in db with: " + uuid + "...")
    DB.withConnection { implicit c =>
      SQL("delete from sessions where uuid = {uuid}").on(
        'uuid -> uuid
      ).executeUpdate()
    }
  }

  def generateUUID(): String = java.util.UUID.randomUUID().toString()
}