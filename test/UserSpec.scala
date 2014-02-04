import org.specs2.mutable._
import models.{User, Session}
import play.api.test.WithApplication

class UserSpec extends Specification
 {
   "User" should {

     val crapUserId = -1
     val userThatDoesntExist = "foobar"

     s"Not exist for crap user id $crapUserId" in new WithApplication {
       User.getById(crapUserId) must beNone
     }

     s"Not exist for crap user name $userThatDoesntExist" in new WithApplication {
       User.getByUsername(userThatDoesntExist) must beNone
     }
   }
 }