import controllers._
import helpers.TestHelpers
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Functional tests and specifications for Application
 */
class ApplicationSpec extends Specification with TestHelpers
{
  "Application" should {
    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/foo")) must beNone
    }
  }

  "Application.index()" should {

    "render the index page without valid credentials" in new WithApplication {
      val index = controllers.Application.index()(FakeRequest())

      status(index) must equalTo(OK)
      contentType(index) must beSome.which(_ == "text/html")
      contentAsString(index) contains "Welcome!" mustEqual true
    }

    "redirect to timeline with valid credentials" in new WithApplication with RandomSessionInsertingAndDeleting {
      val result = controllers.Application.index()(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }
  }
}