import helpers.TestHelpers
import org.specs2.mutable._
import models.Tweetcik
import play.api.test.WithApplication

class TweetcikDeleteSpec extends Specification with TestHelpers
{
  "TweetcikDelete" should {

    s"delete test tweetcik" in new WithApplication with TweetcikInserting {
      Tweetcik.delete(insertedTweetcikId) mustEqual true
    }

    "not be able to delete tweetcik with random id" in new WithApplication {
      Tweetcik.delete(-1) mustEqual false
    }
  }
}