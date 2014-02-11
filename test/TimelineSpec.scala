import controllers._
import helpers.TestHelpers
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Functional tests and specifications for Timeline
 */
class TimelineSpec extends Specification with TestHelpers
{
  "Timeline.renderPage()" should {

    "render the timeline page with valid credentials" in new WithApplication with RandomSessionInsertingAndDeleting {
      val timeline = Timeline.renderPage()(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(timeline) must equalTo(OK)
      contentType(timeline) must beSome.which(_ == "text/html")
      contentAsString(timeline) contains testSession.username mustEqual true
    }

    "redirect to welcome page with invalid credentials" in new WithApplication {
      val result = controllers.Timeline.renderPage()(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }
  }

  "Timeline.submitTweetcik()" should {

    "redirect to timeline page with invalid form data" in new WithApplication {
      val result = controllers.Timeline.submitTweetcik()(FakeRequest()
        .withFormUrlEncodedBody("tweetcik" -> ""))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }

    "submit tweetcik and redirect to timeline with valid form data" in new WithApplication with KnownTweetcikDeleting {
      val result = controllers.Timeline.submitTweetcik()(FakeRequest()
        .withFormUrlEncodedBody("tweetcik" -> testTweetcik.content)
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }

    "redirect to welcome page with invalid credentials" in new WithApplication {
      val result = controllers.Timeline.renderPage()(FakeRequest()
        .withFormUrlEncodedBody("tweetcik" -> "foobar"))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }
  }

  "Timeline.deleteTweetcik()" should {

    "delete tweetcik and redirect to timeline page" in new WithApplication with KnownTweetcikInserting {
      val result = controllers.Timeline.deleteTweetcik(insertedTweetcikId)(FakeRequest()
        .withSession("logged_user" -> testSession.cookieid))

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Timeline.renderPage().toString())
    }

    "redirect to welcome page with invalid credentials" in new WithApplication {
      val result = controllers.Timeline.deleteTweetcik(-1)(FakeRequest())

      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == routes.Application.index().toString())
    }
  }
}