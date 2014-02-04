import org.specs2.mutable._
import models.Session
import play.api.test.WithApplication

class SessionSpec extends Specification
{
  "Session" should {

    val crapCookieId = "foobar"
    val userThatDoesntExist = "foobar"

    s"Not exist for crap cookie id $crapCookieId" in new WithApplication {
      Session.getByCookieId(crapCookieId) must beNone
    }

    s"Not be able to be created for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Session.create(userThatDoesntExist) must throwAn(new IllegalArgumentException())
    }

    s"Affect 0 rows when given crap cookie id $crapCookieId to delete a session" in new WithApplication {
      Session.delete(crapCookieId) must not beGreaterThan 0
    }
  }
}