package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import helpers.SHA512Generator

case class User(username: String, password: String, salt: String)

object User
{
  val user = {
    get[String]("username") ~ get[String]("password") ~ get[String]("salt") map {
      case username ~ password ~ salt => User(username, password, salt)
    }
  }

  def create(username: String, password: String): Option[User] = {
    try {
      DB.withConnection { implicit c =>
        val salt = SHA512Generator.generate(username + System.currentTimeMillis())
        val saltedPassword = SHA512Generator.generate(password + salt)
        SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
          .on('username -> username, 'password -> saltedPassword, 'salt -> salt).executeUpdate()
        val result = SQL("select * from users where username={username} limit 1")
          .on('username -> username, 'password -> saltedPassword, 'salt -> salt)
          .as(user *)
        result.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"User.create() - ${e.getMessage}")
        None
    }
  }

  def read(username: String): Option[User] = {
    try {
      DB.withConnection { implicit c =>
        val users: List[User] = SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(user *)

        users.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"User.read() - ${e.getMessage}")
        None
    }
  }

  def delete(username: String): Boolean = {
    try {
      DB.withConnection { implicit c =>
        if(SQL("delete from users where username = {username}").on('username -> username).executeUpdate() > 0) {
          val result = SQL("select * from users where username={username} limit 1").on('username -> username).as(user *)
          result.isEmpty
        }
        else false
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"User.delete() - ${e.getMessage}")
        false
    }
  }
}