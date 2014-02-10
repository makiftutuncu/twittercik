import helpers.TestHelpers
import org.specs2.mutable._
import models.Session
import play.api.test.WithApplication

class SessionCreateSpec extends Specification with TestHelpers
{
  "SessionCreate" should {

    "create a test session" in new WithApplication with SessionDeleting {
      Session.create(testUser.username) must beSome.which(_.username == testSession.username)
    }

    "not be able to create session with same username" in new WithApplication with SessionInsertingAndDeleting {
      Session.create(testUser.username) must beNone // Try to create the same session again
    }

    "not be able to create session with random username" in new WithApplication {
      Session.create(generateRandomText) must beNone
    }
  }
}