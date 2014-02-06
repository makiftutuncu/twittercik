package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current
import play.api.Logger

case class Tweetcik(id: Long, username: String, content: String, date: Long)
{
  def dateString: String = new java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm:ss").format(new java.util.Date(date))
}

object Tweetcik
{
  val tweetcik = {
    get[Long]("id") ~ get[String]("username") ~ get[String]("content") ~ get[Long]("tweetcikdate") map {
      case id ~ username ~ content ~ date => Tweetcik(id, username, content, date)
    }
  }

  def create(username: String, content: String, date: Long): Boolean = {
    User.read(username) match {
      case Some(user: User) =>
        DB.withConnection { implicit c =>
          try {
            val result = SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {date})")
              .on('username -> username, 'content -> content, 'date -> date)
              .executeUpdate()
            result > 0
          }
          catch {
            case e: Exception =>
              Logger.error(s"Tweetcik.create() - ${e.getMessage}")
              false
          }
        }
      case _ =>
        Logger.info(s"Tweetcik.create() - Cannot create a tweetcik for user that doesn't exist with name $username!")
        false
    }
  }

  def read(username: String): List[Tweetcik] = {
    User.read(username) match {
      case Some(user: User) =>
        try {
          DB.withConnection { implicit c =>
            SQL("select * from tweetciks where username={username}")
              .on('username -> username).as(tweetcik *)
          }
        }
        catch {
          case e: Exception =>
            Logger.error(s"Tweetcik.read() - ${e.getMessage}")
            Nil
        }
      case _ =>
        Logger.info(s"Tweetcik.read() - Cannot read tweetciks of a user that doesn't exist with name $username!")
        Nil
    }
  }

  def readAll: List[Tweetcik] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from tweetciks").as(tweetcik *)
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Tweetcik.readAll() - ${e.getMessage}")
        Nil
    }
  }

  def delete(id: Long): Boolean = {
    try {
      DB.withConnection { implicit c =>
        val result = SQL("delete from tweetciks where id={id}")
          .on('id -> id).executeUpdate()
        result > 0
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Tweetcik.delete() - ${e.getMessage}")
        false
    }
  }
}