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

  def getAllTweetciks: List[Tweetcik] = {
    Logger.debug("Tweetcik.getAllTweetciks() - Getting all twettciks...")
    DB.withConnection { implicit c =>
      SQL("select * from tweetciks").as(tweetcik *)
    }
  }

  def getTweetciksByUsername(username: String): List[Tweetcik] = {
    User.getByUsername(username) match {
      case Some(user: User) =>
        Logger.debug(s"Tweetcik.getTweetciksByUsername() - Getting twettciks of user named $username...")
        DB.withConnection { implicit c =>
          SQL("select * from tweetciks where username={username}").on('username -> username).as(tweetcik *)
        }

      case _ =>
        Logger.error(s"Tweetcik.getTweetciksByUsername() - Cannot get tweetciks of a user that doesn't exist with name $username!")
        throw new IllegalArgumentException()
    }
  }

  def create(username: String, content: String, date: Long) = {
    User.getByUsername(username) match {
      case Some(user: User) =>
        DB.withConnection {
          implicit c =>
            SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {date})").on(
              'username -> username, 'content -> content, 'date -> date).executeUpdate()
        }

      case _ =>
        Logger.error(s"Tweetcik.create() - Cannot create a tweetcik for user that doesn't exist with name $username!")
        throw new IllegalArgumentException()
    }
  }

  def remove(id: Long) = {
    DB.withConnection { implicit c =>
      SQL("delete from tweetciks where id={id}").on('id -> id).executeUpdate()
    }
  }
}