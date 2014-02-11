import controllers._
import helpers.TestHelpers
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Functional tests and specifications for Logout
 */
class LogoutSpec extends Specification with TestHelpers
{
  "Logout.logout()" should {

    "log out and redirect to welcome with valid credentials" in new WithApplication with RandomSessionInsertingAndDeleting {
      val result = controllers.Logout.logout()(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }

    "redirect to welcome with invalid credentials" in new WithApplication {
      val result = controllers.Logout.logout()(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }
  }
}