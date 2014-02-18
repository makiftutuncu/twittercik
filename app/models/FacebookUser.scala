package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Logger
import play.api.Play.current

/**
 * A model for keeping a Facebook user
 *
 * @param userid      Facebook user id of the user
 * @param username    Facebook user name of the user
 * @param accesstoken Facebook access token of the user
 * @param logintime   Time of retrieving access token in milliseconds
 * @param expire      Expire time of access token after the login time in milliseconds
 */
case class FacebookUser(userid: String, username: String, accesstoken: String,
                        logintime: Long, expire: Long)

/**
 * Companion object of FacebookUser acting as data access layer
 */
object FacebookUser
{
  /**
   * A result set parser for Facebook user records in database, maps records to a FacebookUser object
   */
  val facebookUser = {
    get[String]("userid") ~ get[String]("username") ~ get[String]("accesstoken") ~
      get[Long]("logintime") ~ get[Long]("expire") map {
      case userid ~ username ~ accesstoken ~ logintime ~ expire =>
        FacebookUser(userid, username, accesstoken, logintime, expire)
    }
  }

  /**
   * Creates a Facebook user for given information
   *
   * @param userid      Facebook user id of the user
   * @param username    Facebook user name of the user
   * @param accesstoken Facebook access token of the user
   * @param expire      Expire time of access token after the login time in milliseconds
   *
   * @return  An optional FacebookUser if successful
   */
  def create(userid: String, username: String, accesstoken: String, expire: Long): Option[FacebookUser] = {
    try {
      DB.withConnection { implicit c =>
        SQL("insert into facebookusers (userid, username, accesstoken, logintime, expire) values ({userid}, {username}, {accesstoken}, {logintime}, {expire})")
          .on('userid -> userid, 'username -> username, 'accesstoken -> accesstoken, 'logintime -> System.currentTimeMillis(), 'expire -> expire).executeUpdate()
        val result = SQL("select * from facebookusers where userid={userid} limit 1")
          .on('userid -> userid).as(facebookUser *)
        result.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"FacebookUser.create() - ${e.getMessage}")
        None
    }
  }

  /**
   * Reads a Facebook user with given user id from the database
   *
   * @param userid  Facebook user id of the user
   *
   * @return  An optional FacebookUser if successful
   */
  def read(userid: String): Option[FacebookUser] = {
    try {
      DB.withConnection { implicit c =>
        val facebookUsers: List[FacebookUser] = SQL("select * from facebookusers where userid={userid} limit 1")
          .on('userid -> userid).as(facebookUser *)
        facebookUsers.headOption
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"FacebookUser.read() - ${e.getMessage}")
        None
    }
  }

  /**
   * Updates an existing Facebook user for given information
   *
   * @param userid      Facebook user id of the user to update
   * @param accesstoken Facebook access token of the user
   * @param expire      Expire time of access token after the login time in milliseconds
   *
   * @return  An optional FacebookUser if successful
   */
  def update(userid: String, accesstoken: String, expire: Long): Option[FacebookUser] = {
    try {
      DB.withConnection { implicit c =>
        SQL("select * from facebookusers where userid={userid} limit 1")
          .on('userid -> userid).as(facebookUser *).headOption match {
          case Some(existingFacebookUser: FacebookUser) =>
            SQL("update facebookusers set accesstoken={accesstoken}, logintime={logintime}, expire={expire} where userid={userid}")
              .on('accesstoken -> accesstoken, 'logintime -> System.currentTimeMillis(), 'expire -> expire, 'userid -> userid).executeUpdate()
            val result = SQL("select * from facebookusers where userid={userid} limit 1")
              .on('userid -> userid).as(facebookUser *)
            result.headOption
          case _ =>
            Logger.error(s"FacebookUser.update() - Cannot update Facebook user that doesn't exist with user id $userid!")
            None
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"FacebookUser.update() - ${e.getMessage}")
        None
    }
  }

  /**
   * Deletes a Facebook user with given name from the database
   *
   * @param userid  Facebook user id of the user
   *
   * @return  true if successful, false otherwise
   */
  def delete(userid: String): Boolean = {
    try {
      DB.withConnection { implicit c =>
        if(SQL("delete from facebookusers where userid = {userid}")
          .on('userid -> userid).executeUpdate() > 0) {
          val result = SQL("select * from facebookusers where userid={userid} limit 1")
            .on('userid -> userid).as(facebookUser *)
          result.isEmpty
        }
        else false
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"FacebookUser.delete() - ${e.getMessage}")
        false
    }
  }
}