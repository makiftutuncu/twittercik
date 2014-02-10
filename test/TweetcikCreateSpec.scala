import helpers.TestHelpers
import org.specs2.mutable._
import models.Tweetcik
import play.api.test.WithApplication

class TweetcikCreateSpec extends Specification with TestHelpers
{
  "TweetcikCreate" should {

    "create a test tweetcik" in new WithApplication with TweetcikDeleting {
      Tweetcik.create(testTweetcik.username, testTweetcik.content, testTweetcik.date) must beSome.which(t =>
        t.username == testTweetcik.username && t.content == testTweetcik.content && t.date == testTweetcik.date)
    }

    "not be able to create user with random username" in new WithApplication {
      Tweetcik.create(generateRandomText, "", System.currentTimeMillis()) must beNone
    }
  }
}