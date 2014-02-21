package helpers

import scala.util.Random
import models._
import anorm._
import play.api.test.WithApplication
import org.specs2.execute.AsResult
import play.api.db.DB

/**
 * Methods and traits that provide help for tests
 */
object TestHelpers
{
  // ========== General ==========
  /**
   * Generates random text of given length consisting of alphanumerical characters
   *
   * @param length  Length of the text
   *
   * @return Random text of given length consisting of alphanumerical characters
   */
  def generateRandomText(length: Int): String = Random.alphanumeric.take(length).mkString

  /**
   * Generates random text consisting of 12 alphanumerical characters
   *
   * @return Random text consisting of 12 alphanumerical characters
   */
  def generateRandomText: String = generateRandomText(12)

  // ========== User ==========
  /**
   * Generates a dummy User object with random information
   *
   * @return  A dummy User object with random information
   */
  def generateUser: User = User("USERNAME_" + generateRandomText, "PASSWORD_" + generateRandomText, "SALT_" + generateRandomText)

  /**
   * Gives SQL for inserting given user to the database
   *
   * @param user User to insert
   *
   * @return  Resulting SQL
   */
  def insertUserSQL(user: User) = {
    SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
      .on('username -> user.username, 'password -> user.password, 'salt -> user.salt)
  }

  /**
   * Gives SQL for deleting given user from the database
   *
   * @param username Name of the user to delete
   *
   * @return  Resulting SQL
   */
  def deleteUserSQL(username: String) = {
    SQL("delete from users where username={username}")
      .on('username -> username)
  }

  /**
   * An around performing the test after inserting the random test user
   */
  trait RandomUserInserting extends WithApplication
  {
    val testUser: User = generateUser
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
      }
      AsResult(t)
    }
  }

  /**
   * An around performing the test before deleting the random test user
   */
  trait RandomUserDeleting extends WithApplication
  {
    val testUser: User = generateUser
    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the random test user
   */
  trait RandomUserInsertingAndDeleting extends WithApplication
  {
    val testUser: User = generateUser
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around performing the test before deleting the test user with known credentials
   */
  trait KnownUserDeleting extends WithApplication
  {
    val testUser: User = User("USER_" + generateRandomText,
      "153e04547d917f32328360e518fb3f8ee92a26e7ee3ad9fae639113eaafdd6bedb422a1e5503114e55e1aa3debc4c0a771eb3c8618c617c3cb3b5512e8b7110b",
      "a272fac0a70fa47415932e8ff0e33b599cb4dd5056e0bd9f05230b2cca85c1b1a1725bd0f7c9acb55504cdbd3515b52daf07cef762c4357553b3b49184e551b4")
    val nonSaltedPassword = "31668b1d81497fad8da30f92da218e023090f8e6a904b2de5e1d0d679485db043e620505a92cb0f2e643bada19905a289de40bee744f18c2ef60c8f7b22688a1"
    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the test user with known credentials
   */
  trait KnownUserInsertingAndDeleting extends WithApplication
  {
    val testUser: User = User("USER_" + generateRandomText,
      "153e04547d917f32328360e518fb3f8ee92a26e7ee3ad9fae639113eaafdd6bedb422a1e5503114e55e1aa3debc4c0a771eb3c8618c617c3cb3b5512e8b7110b",
      "a272fac0a70fa47415932e8ff0e33b599cb4dd5056e0bd9f05230b2cca85c1b1a1725bd0f7c9acb55504cdbd3515b52daf07cef762c4357553b3b49184e551b4")
    val nonSaltedPassword = "31668b1d81497fad8da30f92da218e023090f8e6a904b2de5e1d0d679485db043e620505a92cb0f2e643bada19905a289de40bee744f18c2ef60c8f7b22688a1"
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser.username).executeUpdate()
      }
      result
    }
  }

  // ========== Session ==========
  /**
   * Generates a dummy Session object with random information for given user
   *
   * @param user  A user for whom a session will be created
   *
   * @return  A dummy Session object with random information
   */
  def generateSession(user: User): Session = Session("COOKIE_ID" + generateRandomText, user.username)

  /**
   * Gives SQL for inserting given session to the database
   *
   * @param session Session to insert
   *
   * @return  Resulting SQL
   */
  def insertSessionSQL(session: Session) = {
    SQL("insert into sessions (cookieid, username) values ({cookieid}, {username})")
      .on('cookieid -> session.cookieid, 'username -> session.username)
  }

  /**
   * Gives SQL for deleting given session from the database
   *
   * @param username Name of the user whose session to delete
   *
   * @return  Resulting SQL
   */
  def deleteSessionSQL(username: String) = {
    SQL("delete from sessions where username = {username}")
      .on('username -> username)
  }

  /**
   * An around performing the test after inserting the random test session for the random test user
   */
  trait RandomSessionInserting extends RandomUserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertSessionSQL(testSession).executeUpdate()
      }
      AsResult(t)
    }
  }

  /**
   * An around performing the test before deleting the random test session for the random test user
   */
  trait RandomSessionDeleting extends RandomUserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the random test session for the random test user
   */
  trait RandomSessionInsertingAndDeleting extends RandomUserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertSessionSQL(testSession).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around performing the test before deleting the test session and the test user with known credentials
   */
  trait KnownSessionAndUserDeleting extends KnownUserDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around performing the test before deleting the test session for the test user with known credentials
   */
  trait KnownSessionDeleting extends KnownUserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the test session for the test user with known credentials
   */
  trait KnownSessionInsertingAndDeleting extends KnownUserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)
    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertSessionSQL(testSession).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession.username).executeUpdate()
      }
      result
    }
  }

  // ========== Tweetcik ==========
  /**
   * Generates a dummy Tweetcik object with random information for given user
   *
   * @param user  A user for whom a tweetcik will be created
   *
   * @return  A dummy Tweetcik object with random information
   */
  def generateTweetcik(user: User): Tweetcik = Tweetcik(-1, user.username, generateRandomText(140), System.currentTimeMillis())

  /**
   * Gives SQL for inserting given tweetcik to the database
   *
   * @param tweetcik Tweetcik to insert
   *
   * @return  Resulting SQL
   */
  def insertTweetcikSQL(tweetcik: Tweetcik) = {
    SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {tweetcikdate})")
      .on('username -> tweetcik.username, 'content -> tweetcik.content, 'tweetcikdate -> tweetcik.date)
  }

  /**
   * Gives SQL for deleting tweetcik with given id from the database
   *
   * @param id Id of the tweetcik to delete
   *
   * @return  Resulting SQL
   */
  def deleteTweetcikSQL(id: Long) = {
    SQL("delete from tweetciks where id={id}")
      .on('id -> id)
  }

  /**
   * An around performing the test after inserting the random test tweetcik for the random test user
   */
  trait RandomTweetcikInserting extends RandomUserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    def getInsertedTweetcik: Option[Tweetcik]

    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertTweetcikSQL(testTweetcik).executeUpdate()
      }
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) => // This is OK
        case _ => throw new Exception("Could not insert the test tweetcik!")
      }
      AsResult(t)
    }
  }

  /**
   * An around performing the test before deleting the random test tweetcik for the random test user
   */
  trait RandomTweetcikDeleting extends RandomUserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    def getInsertedTweetcik: Option[Tweetcik]

    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) =>
          DB.withConnection { implicit c =>
            deleteTweetcikSQL(tweetcik.id).executeUpdate()
          }
        case _ => throw new Exception("Could not delete the test tweetcik!")
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the random test tweetcik for the random test user
   */
  trait RandomTweetcikInsertingAndDeleting extends RandomUserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    def getInsertedTweetcik: Option[Tweetcik] = {
      DB.withConnection { implicit c =>
        SQL("select * from tweetciks where username={username} limit 1")
          .on('username -> testTweetcik.username).as(Tweetcik.tweetcik *).headOption
      }
    }

    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertTweetcikSQL(testTweetcik).executeUpdate()
      }
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) => // This is OK
        case _ => throw new Exception("Could not insert the test tweetcik!")
      }
      val result = AsResult(t)
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) =>
          DB.withConnection { implicit c =>
            deleteTweetcikSQL(tweetcik.id).executeUpdate()
          }
        case _ => throw new Exception("Could not delete the test tweetcik!")
      }
      result
    }
  }

  /**
   * An around performing the test after inserting the test tweetcik with the test session for the test user
   */
  trait KnownTweetcikInserting extends RandomSessionInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    def getInsertedTweetcik: Option[Tweetcik]

    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertTweetcikSQL(testTweetcik).executeUpdate()
      }
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) => // This is OK
        case _ => throw new Exception("Could not insert the test tweetcik!")
      }
      AsResult(t)
    }
  }

  /**
   * An around performing the test before deleting the test tweetcik with the test session for the test user
   */
  trait KnownTweetcikDeleting extends RandomSessionInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    def getInsertedTweetcik: Option[Tweetcik]

    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      getInsertedTweetcik match {
        case Some(tweetcik: Tweetcik) =>
          DB.withConnection { implicit c =>
            deleteTweetcikSQL(testTweetcik.id).executeUpdate()
          }
        case _ => throw new Exception("Could not delete the test tweetcik!")
      }
      result
    }
  }

  // ========== FacebookUser ==========
  /**
   * Generates a dummy FacebookUser object with random information
   *
   * @return  A dummy FacebookUser object with random information
   */
  def generateFacebookUser: FacebookUser = {
    val username = "USERNAME_" + generateRandomText
    FacebookUser(username, "FBUSERID_" + generateRandomText,
      "FBACCESSTOKEN_" + generateRandomText(128), System.currentTimeMillis(), 1234567)
  }

  /**
   * Gives SQL for inserting given Facebook user to the database
   *
   * @param facebookUser FacebookUser to insert
   *
   * @return  Resulting SQL
   */
  def insertFacebookUserSQL(facebookUser: FacebookUser) = {
    SQL("insert into facebookusers (userid, username, accesstoken, logintime, expire) values ({userid}, {username}, {accesstoken}, {logintime}, {expire})")
      .on('userid -> facebookUser.userid, 'username -> facebookUser.username,
        'accesstoken -> facebookUser.accesstoken, 'logintime -> facebookUser.logintime,
        'expire -> facebookUser.expire)
  }

  /**
   * Gives SQL for deleting given Facebook user from the database
   *
   * @param userid User id of the Facebook user to delete
   *
   * @return  Resulting SQL
   */
  def deleteFacebookUserSQL(userid: String) = {
    SQL("delete from facebookusers where userid={userid}")
      .on('userid -> userid)
  }

  /**
   * An around performing the test after inserting the random test Facebook user
   */
  trait RandomFacebookUserInserting extends WithApplication
  {
    val testFacebookUser: FacebookUser = generateFacebookUser
    val testUser: User = User(testFacebookUser.username, "PASSWORD_" + generateRandomText, "SALT_" + generateRandomText)

    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
        insertFacebookUserSQL(testFacebookUser).executeUpdate()
      }
      AsResult(t)
    }
  }

  /**
   * An around performing the test before deleting the random test Facebook user
   */
  trait RandomFacebookUserDeleting extends WithApplication
  {
    val testFacebookUser: FacebookUser = generateFacebookUser

    override def around[T : AsResult](t: => T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteFacebookUserSQL(testFacebookUser.userid).executeUpdate()
        deleteUserSQL(testFacebookUser.username).executeUpdate()
      }
      result
    }
  }

  /**
   * An around wrapping the test between inserting and deleting the random test Facebook user
   */
  trait RandomFacebookUserInsertingAndDeleting extends WithApplication
  {
    val testFacebookUser: FacebookUser = generateFacebookUser
    val testUser: User = User(testFacebookUser.username, "PASSWORD_" + generateRandomText, "SALT_" + generateRandomText)

    override def around[T : AsResult](t: => T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
        insertFacebookUserSQL(testFacebookUser).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteFacebookUserSQL(testFacebookUser.userid).executeUpdate()
        deleteUserSQL(testUser.username).executeUpdate()
      }
      result
    }
  }
}