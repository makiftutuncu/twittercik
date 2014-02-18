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
 * @param fbuserid  Facebook user id of the user
 */
case class User(username: String, password: String, salt: String, fbuserid: Option[String] = None)

/**
 * Companion object of User acting as data access layer
 */
object User
{
  /**
   * A result set parser for user records in database, maps records to a User object
   */
  val user = {
    get[String]("username") ~ get[String]("password") ~ get[String]("salt") ~ get[Option[String]]("fbuserid") map {
      case username ~ password ~ salt ~ fbuserid => User(username, password, salt, fbuserid)
    }
  }

  /**
   * Creates a user for given information in the database
   *
   * @param username  Name of the user
   * @param password  Hashed value of the password user entered
   * @param fbuserid  Facebook user id of the user
   *
   * @return  An optional User if successful
   */
  def create(username: String, password: String, fbuserid: Option[String] = None): Option[User] = {
    try {
      DB.withConnection { implicit c =>
        val salt = SHA512Generator.generate(username + System.currentTimeMillis())
        val saltedPassword = SHA512Generator.generate(password + salt)
        SQL("insert into users (username, password, salt, fbuserid) values ({username}, {password}, {salt}, {fbuserid})")
          .on('username -> username, 'password -> saltedPassword, 'salt -> salt, 'fbuserid -> fbuserid).executeUpdate()
        val result = SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(user *)
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
   * Reads a user with given Facebook user id from the database
   *
   * @param fbuserid  Facebook user id of the user
   *
   * @return  An optional User if successful
   */
  def readWithFacebookUserId(fbuserid: String): Option[User] = {
    try {
      DB.withConnection { implicit c =>
        val users: List[User] = SQL("select * from users where fbuserid={fbuserid} limit 1")
          .on('fbuserid -> fbuserid).as(user *)
        users.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"User.readWithFacebookUserId() - ${e.getMessage}")
        None
    }
  }

  /**
   * Updates a users Facebook user id for given information in the database
   *
   * @param username  Name of the user
   * @param fbuserid  Facebook user id of the user
   *
   * @return  An optional User if successful
   */
  def updateFacebookUserId(username: String, fbuserid: String): Option[User] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from users where username={username} limit 1")
          .on('username -> username).as(user *).headOption match {
          case Some(existingUser: User) =>
            SQL("update users set fbuserid={fbuserid} where username={username}")
              .on('fbuserid -> fbuserid, 'username -> username).executeUpdate()
            val result = SQL("select * from users where username={username} limit 1")
              .on('username -> username).as(user *)
            result.headOption
          case _ =>
            Logger.error(s"User.updateFacebookUserId() - Cannot update Facebook user id of a user that doesn't exist with user id $username!")
            None
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"User.updateFacebookUserId() - ${e.getMessage}")
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
        if(SQL("delete from users where username = {username}")
          .on('username -> username).executeUpdate() > 0) {
          val result = SQL("select * from users where username={username} limit 1")
            .on('username -> username).as(user *)
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