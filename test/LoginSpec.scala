import controllers._
import helpers.TestHelpers._
import models.User
import org.specs2.mutable._
import play.api.db.DB
import play.api.test._
import play.api.test.Helpers._
import anorm._

/**
 * Functional tests and specifications for Login
 */
class LoginSpec extends Specification
{
  "Login.renderPage()" should {

    "render the login page without valid credentials" in new WithApplication {
      val login = controllers.Login.renderPage()(FakeRequest())

      status(login) must equalTo(OK)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) contains "Login" mustEqual true
    }

    "redirect to timeline with valid credentials" in new WithApplication with RandomSessionInsertingAndDeleting {
      val result = controllers.Application.index()(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }
  }

  "Login.submitLoginForm()" should {

    "result in a bad request with invalid form data and show login page again" in new WithApplication {
      val login = controllers.Login.submitLoginForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> "",
        "hashedpassword" -> ""
      ))

      status(login) must equalTo(BAD_REQUEST)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) contains "Login" mustEqual true
    }

    "log user in and redirect to timeline for valid username/password" in new WithApplication with KnownSessionDeleting {
      val result = controllers.Login.submitLoginForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> testUser.username,
        "hashedpassword" -> nonSaltedPassword
      ))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())

      DB.withConnection { implicit c =>
        val maybeUser = SQL("select * from users where username={username} limit 1")
          .on('username -> testUser.username).as(User.user *).headOption

        maybeUser must beSome[User]
        maybeUser.get.username mustEqual testUser.username
      }
    }

    "not be able to log user in since user is already logged in and redirect to welcome" in new WithApplication with KnownSessionInsertingAndDeleting {
      // At this point there will already be a session for the logged user
      val result = controllers.Login.submitLoginForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> testUser.username,
        "hashedpassword" -> nonSaltedPassword
      ))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }

    "result in a bad request and show login page again for password that doesn't match" in new WithApplication with RandomUserInsertingAndDeleting {
      val login = controllers.Login.submitLoginForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> testUser.username,
        "hashedpassword" -> "foobar"
      ))

      status(login) must equalTo(BAD_REQUEST)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) contains "Login" mustEqual true
    }

    "result in a bad request and show login page again for username that doesn't exist" in new WithApplication with RandomUserInsertingAndDeleting {
      val login = controllers.Login.submitLoginForm()(FakeRequest().withFormUrlEncodedBody(
        "username" -> "foobar",
        "hashedpassword" -> "foobar"
      ))

      status(login) must equalTo(BAD_REQUEST)
      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) contains "Login" mustEqual true
    }
  }
}