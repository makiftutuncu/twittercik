package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import helpers.SHA512Generator

/**
 * A model for keeping a user
 *
 * @param username  Name of the user
 * @param password  Salted and hashed value of password of the user
 * @param salt      Salt to combine with the hashed value of the password user entered
 */
case class User(username: String, password: String, salt: String)

/**
 * Companion object of User acting as data access layer
 */
object User
{
  /**
   * A result set parser for user records in database, maps records to a User object
   */
  val user = {
    get[String]("username") ~ get[String]("password") ~ get[String]("salt") map {
      case username ~ password ~ salt => User(username, password, salt)
    }
  }

  /**
   * Creates a user for given information in the database
   *
   * @param username  Name of the user
   * @param password  Hashed value of the password user entered
   *
   * @return  An optional User if successful
   */
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

  /**
   * Reads a user with given name from the database
   *
   * @param username  Name of the user
   *
   * @return  An optional User if successful
   */
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

  /**
   * Deletes a user with given name from the database
   *
   * @param username  Name of the user to delete
   *
   * @return  true if successful, false otherwise
   */
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