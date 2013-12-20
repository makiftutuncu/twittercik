package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import anorm.~
import play.api.Play.current

case class Tweetcik(id: Long, username: String, content: String, date: Long)
{
  def dateString: String = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(new java.util.Date(date))
}

object Tweetcik
{
  val tweetcik = {
    get[Long]("id") ~ get[String]("username") ~ get[String]("content") ~ get[Long]("tweetcikdate") map {
      case id ~ username ~ content ~ date => Tweetcik(id, username, content, date)
    }
  }

  def getAllTweetciks(): List[Tweetcik] = {
    println("Tweetcik.getAllTweetciks() - Getting all twettciks...")
    DB.withConnection { implicit c =>
      SQL("select * from tweetciks").as(tweetcik *)
    }
  }

  def create(username: String, content: String, date: Long) = {
    println("Tweetcik.create() - Creating a tweetcik as \"" + content + "\" for username " + username + "...")
    DB.withConnection { implicit c =>
      SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {date})").on(
        'username -> username, 'content -> content, 'date -> date).executeUpdate()
    }
  }
}