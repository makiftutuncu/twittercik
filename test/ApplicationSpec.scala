import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification
{
  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/foo")) must beNone
    }

    "render the index page without a session" in new WithApplication {
      val index = controllers.Application.index()(FakeRequest())

      status(index) must equalTo(OK)
      contentType(index) must beSome.which(_ == "text/html")
      contentAsString(index) contains "Welcome!" mustEqual true
    }
  }
}