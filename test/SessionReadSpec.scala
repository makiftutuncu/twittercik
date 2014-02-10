import helpers.TestHelpers
import org.specs2.mutable._
import models.Session
import play.api.test.WithApplication

class SessionReadSpec extends Specification with TestHelpers
{
  "SessionRead" should {

    s"read test session" in new WithApplication with SessionInsertingAndDeleting {
      Session.read(testSession.cookieid) must beSome(testSession)
    }

    "not be able to read session with random cookieid" in new WithApplication {
      Session.read(generateRandomText) must beNone
    }
  }
}