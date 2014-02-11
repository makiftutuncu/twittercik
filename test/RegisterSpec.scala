import controllers._
import helpers.TestHelpers
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Functional tests and specifications for Register
 */
class RegisterSpec extends Specification with TestHelpers
{
  "Register.renderPage()" should {

    "render the register page without valid credentials" in new WithApplication {
      val register = controllers.Register.renderPage()(FakeRequest())

      status(register) must equalTo(OK)
      contentType(register) must beSome.which(_ == "text/html")
      contentAsString(register) contains "Register" mustEqual true
    }

    "redirect to timeline with valid credentials" in new WithApplication with RandomSessionInsertingAndDeleting {
      val result = controllers.Register.renderPage()(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }
  }

  "Register.submitRegisterForm()" should {

    "result in a bad request with invalid form data and show register page again" in new WithApplication {
      val register = controllers.Register.submitRegisterForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> "",
        "hashedpassword" -> ""
      ))

      status(register) must equalTo(BAD_REQUEST)
      contentType(register) must beSome.which(_ == "text/html")
      contentAsString(register) contains "Register" mustEqual true
    }

    "result in a bad request with existing username and show register page again" in new WithApplication with RandomUserInsertingAndDeleting {
      val register = controllers.Register.submitRegisterForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> testUser.username,
        "hashedpassword" -> "foobar"
      ))

      status(register) must equalTo(BAD_REQUEST)
      contentType(register) must beSome.which(_ == "text/html")
      contentAsString(register) contains "Register" mustEqual true
    }

    "register, log user in and redirect to timeline for valid username/password" in new WithApplication with KnownSessionAndUserDeleting {
      val result = controllers.Register.submitRegisterForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> testUser.username,
        "hashedpassword" -> nonSaltedPassword
      ))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }
  }
}