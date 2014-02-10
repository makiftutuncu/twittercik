package helpers

import scala.util.Random
import models.{Tweetcik, Session, User}
import anorm._
import play.api.test.WithApplication
import org.specs2.execute.AsResult
import play.api.db.DB

trait TestHelpers
{
  def generateRandomText(length: Int): String = Random.alphanumeric.take(length).mkString
  def generateRandomText: String = generateRandomText(12)
  
  def generateUser: User = User("USERNAME_" + generateRandomText, "PASSWORD_" + generateRandomText, "SALT_" + generateRandomText)
  def generateSession(user: User): Session = Session("COOKIE_ID" + generateRandomText, user.username)
  def generateTweetcik(user: User): Tweetcik = Tweetcik(-1, user.username, generateRandomText(140), System.currentTimeMillis())

  def insertUserSQL(user: User) = {
    SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
      .on('username -> user.username, 'password -> user.password, 'salt -> user.salt)
  }
  
  def deleteUserSQL(user: User) = {
    SQL("delete from users where username={username}")
      .on('username -> user.username)
  }

  def insertSessionSQL(session: Session) = {
    SQL("insert into sessions (cookieid, username) values ({cookieid}, {username})")
      .on('cookieid -> session.cookieid, 'username -> session.username)
  }

  def deleteSessionSQL(session: Session) = {
    SQL("delete from sessions where username = {username}")
      .on('username -> session.username)
  }

  def insertTweetcikSQL(tweetcik: Tweetcik) = {
    SQL("insert into tweetciks (username, content, tweetcikdate) values ({username}, {content}, {tweetcikdate})")
      .on('username -> tweetcik.username, 'content -> tweetcik.content, 'tweetcikdate -> tweetcik.date)
  }

  def deleteTweetcikSQL(id: Long) = {
    SQL("delete from tweetciks where id={id}")
      .on('id -> id)
  }

  trait UserInsertingAndDeleting extends WithApplication
  {
    val testUser: User = generateUser

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser).executeUpdate()
      }
      result
    }
  }

  trait UserInserting extends WithApplication
  {
    val testUser: User = generateUser

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertUserSQL(testUser).executeUpdate()
      }
      AsResult(t)
    }
  }

  trait UserDeleting extends WithApplication
  {
    val testUser: User = generateUser

    override def around[T : AsResult](t: =>T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteUserSQL(testUser).executeUpdate()
      }
      result
    }
  }

  trait SessionInsertingAndDeleting extends UserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertSessionSQL(testSession).executeUpdate()
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession).executeUpdate()
      }
      result
    }
  }

  trait SessionInserting extends UserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertSessionSQL(testSession).executeUpdate()
      }
      AsResult(t)
    }
  }

  trait SessionDeleting extends UserInsertingAndDeleting
  {
    val testSession: Session = generateSession(testUser)

    override def around[T : AsResult](t: =>T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteSessionSQL(testSession).executeUpdate()
      }
      result
    }
  }

  trait TweetcikInsertingAndDeleting extends UserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    var insertedTweetcikId: Long = -1

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertTweetcikSQL(testTweetcik).executeUpdate()
        SQL("select * from tweetciks where username={username} and tweetcikdate={tweetcikdate} limit 1")
          .on('username -> testTweetcik.username, 'tweetcikdate -> testTweetcik.date)
          .as(Tweetcik.tweetcik *).headOption match {
          case Some(tweetcik: Tweetcik) =>
            insertedTweetcikId = tweetcik.id
          case _ =>
            insertedTweetcikId = -1
        }
      }
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        deleteTweetcikSQL(insertedTweetcikId).executeUpdate()
      }
      result
    }
  }

  trait TweetcikInserting extends UserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    var insertedTweetcikId: Long = -1

    override def around[T : AsResult](t: =>T) = super.around {
      DB.withConnection { implicit c =>
        insertTweetcikSQL(testTweetcik).executeUpdate()
        SQL("select * from tweetciks where username={username} and tweetcikdate={tweetcikdate} limit 1")
          .on('username -> testTweetcik.username, 'tweetcikdate -> testTweetcik.date)
          .as(Tweetcik.tweetcik *).headOption match {
          case Some(tweetcik: Tweetcik) =>
            insertedTweetcikId = tweetcik.id
          case _ =>
            insertedTweetcikId = -1
        }
      }
      AsResult(t)
    }
  }

  trait TweetcikDeleting extends UserInsertingAndDeleting
  {
    val testTweetcik: Tweetcik = generateTweetcik(testUser)
    var insertedTweetcikId: Long = -1

    override def around[T : AsResult](t: =>T) = super.around {
      val result = AsResult(t)
      DB.withConnection { implicit c =>
        SQL("select * from tweetciks where username={username} and tweetcikdate={tweetcikdate} limit 1")
          .on('username -> testTweetcik.username, 'tweetcikdate -> testTweetcik.date)
          .as(Tweetcik.tweetcik *).headOption match {
          case Some(tweetcik: Tweetcik) =>
            insertedTweetcikId = tweetcik.id
          case _ =>
            insertedTweetcikId = -1
        }
        deleteTweetcikSQL(insertedTweetcikId).executeUpdate()
      }
      result
    }
  }
}