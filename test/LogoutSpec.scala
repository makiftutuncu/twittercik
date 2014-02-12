import anorm._
import controllers._
import helpers.TestHelpers
import models.Session
import org.specs2.mutable._
import play.api.db.DB
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

      DB.withConnection { implicit c =>
        SQL("select * from sessions where cookieid={cookieid} limit 1")
          .on('cookieid -> testSession.cookieid).as(Session.session *).headOption must beNone
      }
    }

    "redirect to welcome with invalid credentials" in new WithApplication {
      val result = controllers.Logout.logout()(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }
  }
}