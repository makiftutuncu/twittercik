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

  def create(username: String, password: String): Boolean = {
    DB.withConnection { implicit c =>
      val salt = SHA512Generator.generate(username + System.currentTimeMillis())
      val saltedPassword = SHA512Generator.generate(password + salt)
      val result = SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
        .on('username -> username, 'password -> saltedPassword, 'salt -> salt)
        .executeUpdate()
      (result > 0)
    }
  }

  def read(username: String): Option[User] = {
    DB.withConnection { implicit c =>
      val users: List[User] = SQL("select * from users where username={username} limit 1")
        .on('username -> username).as(user *)

      val result = users.headOption
      result match {
        case Some(_) => Logger.debug(s"User.read() - User named ${username} is found, returning it...")
        case _ => Logger.info(s"User.read() - User named ${username} is not found!")
      }
      result
    }
  }

  def delete(username: String): Boolean = {
    DB.withConnection { implicit c =>
      val result = SQL("delete from users where username = {username}").on('username -> username).executeUpdate()
      (result > 0)
    }
  }
}