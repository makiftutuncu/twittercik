import anorm._
import helpers.TestHelpers
import org.specs2.mutable._
import models.Tweetcik
import play.api.db.DB
import play.api.test.WithApplication

/**
 * Integration tests and specifications for Tweetcik
 */
class TweetcikSpec extends Specification with TestHelpers
{
  "Tweetcik.create()" should {

    "create a test tweetcik" in new WithApplication with RandomTweetcikDeleting {
      Tweetcik.create(testTweetcik.username, testTweetcik.content, testTweetcik.date) must beSome.which(t =>
        t.username == testTweetcik.username && t.content == testTweetcik.content && t.date == testTweetcik.date)

      val maybeTweetcik = getInsertedTweetcik
      maybeTweetcik must beSome[Tweetcik]
      maybeTweetcik.get.content mustEqual testTweetcik.content

      override def getInsertedTweetcik: Option[Tweetcik] = {
        DB.withConnection { implicit c =>
          SQL("select * from tweetciks where username={username} limit 1")
            .on('username -> testTweetcik.username).as(Tweetcik.tweetcik *).headOption
        }
      }
    }

    "not be able to create user with random username" in new WithApplication {
      Tweetcik.create(generateRandomText, "", System.currentTimeMillis()) must beNone
    }
  }

  "Tweetcik.read()" should {

    s"read test tweetciks of a user" in new WithApplication with RandomTweetcikInsertingAndDeleting {
      Tweetcik.read(testUser.username).size mustEqual 1
    }

    s"read all tweetciks" in new WithApplication with RandomTweetcikInsertingAndDeleting {
      Tweetcik.readAll.size must beGreaterThanOrEqualTo(1)
    }

    "not be able to read tweetciks with random username" in new WithApplication {
      Tweetcik.read(generateRandomText) must beEmpty
    }
  }

  "Tweetcik.delete()" should {

    s"delete test tweetcik" in new WithApplication with RandomTweetcikInserting {
      val maybeTweetcik = getInsertedTweetcik
      maybeTweetcik must beSome[Tweetcik]

      Tweetcik.delete(maybeTweetcik.get.id) mustEqual true

      getInsertedTweetcik must beNone

      override def getInsertedTweetcik: Option[Tweetcik] = {
        DB.withConnection { implicit c =>
          SQL("select * from tweetciks where username={username} limit 1")
            .on('username -> testTweetcik.username).as(Tweetcik.tweetcik *).headOption
        }
      }
    }

    "not be able to delete tweetcik with random id" in new WithApplication {
      Tweetcik.delete(-1) mustEqual false
    }
  }
}