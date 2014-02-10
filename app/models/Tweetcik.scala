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

  def create(username: String, content: String, date: Long): Option[Tweetcik] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(User.user *) match {
          case users: List[User] =>
            if(users.isEmpty) {
              Logger.error(s"Tweetcik.create() - Cannot create a tweetcik for user that doesn't exist with name $username!")
              None
            }
            else {
              SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {tweetcikdate})")
                .on('username -> username, 'content -> content, 'tweetcikdate -> date)
                .executeUpdate()
              val result = SQL("select * from tweetciks where username={username} and tweetcikdate={tweetcikdate} limit 1")
                .on('username -> username, 'tweetcikdate -> date).as(tweetcik *)
              result.headOption
            }
          case _ =>
            Logger.error(s"Tweetcik.create() - Cannot create a tweetcik for user that doesn't exist with name $username!")
            None
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Tweetcik.create() - ${e.getMessage}")
        None
    }
  }

  def read(username: String): List[Tweetcik] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(User.user *) match {
          case users: List[User] =>
            if(users.isEmpty) {
              Logger.error(s"Tweetcik.read() - Cannot read tweetciks of a user that doesn't exist with name $username!")
              Nil
            }
            else {
              SQL("select * from tweetciks where username={username}")
                .on('username -> username).as(tweetcik *)
            }
          case _ =>
            Logger.error(s"Tweetcik.read() - Cannot read tweetciks of a user that doesn't exist with name $username!")
            Nil
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Tweetcik.read() - ${e.getMessage}")
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