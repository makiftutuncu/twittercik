package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(username: String, password: String)

object User
{
  val user = {
    get[String]("username") ~ get[String]("password") map {
      case username ~ password => User(username, password)
    }
  }

  def getUser(username: String): Option[User] = {
    DB.withConnection { implicit c =>
      val users: List[User] = SQL("select * from users where username={username}").on(
        'username -> username
      ).as(user *)

      if(users.length != 0)
      {
        println("User.getUser() - Found user in db with username: " + username)
        users.headOption
      }
      else
      {
        println("User.getUser() - Couldn't find user in db with username: " + username)
        None
      }
    }
  }

  def create(username: String, password: String) = {
    println("User.create() - Creating a user in db with username " + username + " and password " + password + "...")
    DB.withConnection { implicit c =>
      SQL("insert into users (username, password) values ({username}, {password})").on(
        'username -> username, 'password -> password
      ).executeUpdate()
    }
  }
}