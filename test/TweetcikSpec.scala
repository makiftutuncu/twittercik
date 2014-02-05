import helpers.SHA512Generator
import org.specs2.mutable._
import models.{User, Tweetcik}
import play.api.test.WithApplication

class TweetcikSpec extends Specification
{
  val testUserName = "test"
  val testUserPassword = SHA512Generator.generate("123456")
  var testTweetcikId: Long = -1
  val crapTweetcikId = -1
  val userThatDoesntExist = "foobar"

  "Tweetcik" should {

    "create a test user and then create a tweetcik for test user" in new WithApplication {
      if(User.create(testUserName, testUserPassword))
        Tweetcik.create(testUserName, "This is a test tweetcik.", System.currentTimeMillis()) mustEqual true
      else failure("Cannot create test user!")
    }

    "read tweetciks for test user" in new WithApplication {
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

    s"delete test user and then delete tweetcik with id $testTweetcikId of test user" in new WithApplication {
      if(User.delete(testUserName))
        Tweetcik.delete(testTweetcikId) mustEqual true
      else failure("Cannot delete test user!")
    }

    s"not be able to create for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Tweetcik.create(userThatDoesntExist, "foobar", System.currentTimeMillis()) mustEqual false
    }

    s"not be able to read tweetciks for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Tweetcik.read(userThatDoesntExist) mustEqual Nil
    }

    s"not be able to delete a tweetcik with crap tweetcik id $crapTweetcikId" in new WithApplication {
      Tweetcik.delete(crapTweetcikId) mustEqual false
    }
  }
}