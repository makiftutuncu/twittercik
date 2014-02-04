import org.specs2.mutable._
import models.{Tweetcik, Session}
import play.api.test.WithApplication

class TweetcikSpec extends Specification
 {
   "Tweetcik" should {

     val crapTweetcikId = -1
     val userThatDoesntExist = "foobar"

     s"Not be able to get tweetciks for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
       Tweetcik.getTweetciksByUsername(userThatDoesntExist) must throwAn(new IllegalArgumentException())
     }

     s"Not be able to be created for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
       Tweetcik.create(userThatDoesntExist, "foobar", System.currentTimeMillis()) must throwAn(new IllegalArgumentException())
     }

     s"Affect 0 rows when given crap tweetcik id $crapTweetcikId to remove a tweetcik" in new WithApplication {
       Tweetcik.remove(crapTweetcikId) must not beGreaterThan 0
     }
   }
 }