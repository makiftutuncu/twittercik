import anorm._
import helpers.SHA512Generator
import org.specs2.mutable._
import models.Tweetcik
import play.api.db.DB
import play.api.test.{FakeApplication, WithApplication}
import scala.Some

class TweetcikSpec extends Specification
{
  "Tweetcik" should {

    step(before)

    "create a tweetcik for test user" in new WithApplication {
      Tweetcik.create(testUserName, "This is a test tweetcik.", System.currentTimeMillis()) mustEqual true
    }

    "read tweetciks of test user" in new WithApplication {
      val list = Tweetcik.read(testUserName)
      list.headOption match {
        case Some(tweetcik: Tweetcik) =>
          testTweetcikId = tweetcik.id
          list mustNotEqual Nil
        case _ => failure("Cannot read tweetciks for test user!")
      }
    }

    "read all tweetciks" in new WithApplication {
      Tweetcik.readAll mustNotEqual Nil
    }

    s"delete tweetcik with id $testTweetcikId of test user" in new WithApplication {
      Tweetcik.delete(testTweetcikId) mustEqual true
    }

    step(after)

    s"not be able to create for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Tweetcik.create(userThatDoesntExist, "foobar", System.currentTimeMillis()) mustEqual false
    }

    s"not be able to read tweetciks of a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Tweetcik.read(userThatDoesntExist) mustEqual Nil
    }

    s"not be able to delete a tweetcik with crap tweetcik id $crapTweetcikId" in new WithApplication {
      Tweetcik.delete(crapTweetcikId) mustEqual false
    }
  }

  implicit val app = FakeApplication()
  val testUserName = "test"
  val testUserPassword = SHA512Generator.generate("123456")
  var testTweetcikId: Long = -1
  val crapTweetcikId = -1
  val userThatDoesntExist = "foobar"

  def before = {
    DB.withConnection { implicit c =>
      try {
        val salt = SHA512Generator.generate(testUserName + System.currentTimeMillis())
        val saltedPassword = SHA512Generator.generate(testUserPassword + salt)
        SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
          .on('username -> testUserName, 'password -> saltedPassword, 'salt -> salt)
          .executeUpdate()
      }
      catch {
        case e: Exception => failure(e.getMessage)
      }
    }
  }

  def after = {
    try {
      DB.withConnection { implicit c =>
        SQL("delete from users where username = {username}").on('username -> testUserName)
          .executeUpdate()
      }
    }
    catch {
      case e: Exception => failure(e.getMessage)
    }
  }
}