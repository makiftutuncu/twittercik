package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import helpers.SHA512Generator

case class User(id: Long, username: String, password: String, salt: String)

object User
{
  val user = {
    get[Long]("id") ~ get[String]("username") ~ get[String]("password") ~ get[String]("salt") map {
      case id ~ username ~ password ~ salt => User(id, username, password, salt)
    }
  }

  def getById(id: Long): Option[User] = {
    DB.withConnection { implicit c =>
      val users: List[User] = SQL("select * from users where id={id} limit 1").on('id -> id).as(user *)

      if(users.length == 1)
      {
        Logger.debug(s"User.getById() - User with id ${id} is found, returning it...")
        users.headOption
      }
      else
      {
        Logger.info(s"User.getById() - User with id ${id} is not found!")
        None
      }
    }
  }

  def getByUsername(username: String): Option[User] = {
    DB.withConnection { implicit c =>
      val users: List[User] = SQL("select * from users where username={username} limit 1").on('username -> username).as(user *)

      if(users.length == 1)
      {
        Logger.debug(s"User.getByUsername() - User named ${username} is found, returning it...")
        users.headOption
      }
      else
      {
        Logger.info(s"User.getByUsername() - User named ${username} is not found!")
        None
      }
    }
  }

  def create(username: String, password: String): Int = {
    DB.withConnection { implicit c =>
      val salt = SHA512Generator.generate(username + System.currentTimeMillis())
      val saltedPassword = SHA512Generator.generate(password + salt)
      SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})").on(
        'username -> username, 'password -> saltedPassword, 'salt -> salt
      ).executeUpdate()
    }
  }
}