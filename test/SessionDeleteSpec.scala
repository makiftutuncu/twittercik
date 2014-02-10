import helpers.TestHelpers
import org.specs2.mutable._
import models.Session
import play.api.test.WithApplication

class SessionDeleteSpec extends Specification with TestHelpers
{
  "SessionDelete" should {

    s"delete test session" in new WithApplication with SessionInserting {
      Session.delete(testSession.cookieid) mustEqual true
    }

    "not be able to delete session with random cookieid" in new WithApplication {
      Session.delete(generateRandomText) mustEqual false
    }
  }
}