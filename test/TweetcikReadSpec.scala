import helpers.TestHelpers
import org.specs2.mutable._
import models.Tweetcik
import play.api.test.WithApplication

class TweetcikReadSpec extends Specification with TestHelpers
{
  "TweetcikRead" should {

    s"read test tweetciks of a user" in new WithApplication with TweetcikInsertingAndDeleting {
      Tweetcik.read(testUser.username).size mustEqual 1
    }

    s"read all tweetciks" in new WithApplication with TweetcikInsertingAndDeleting {
      Tweetcik.readAll.size must beGreaterThanOrEqualTo(1)
    }

    "not be able to read tweetciks with random username" in new WithApplication {
      Tweetcik.read(generateRandomText) must beEmpty
    }
  }
}