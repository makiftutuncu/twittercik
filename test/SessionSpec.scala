import anorm._
import helpers.TestHelpers._
import org.specs2.mutable._
import models.Session
import play.api.db.DB
import play.api.test.WithApplication

/**
 * Integration tests and specifications for Session
 */
class SessionSpec extends Specification
{
  "Session.create()" should {

    "create a test session" in new WithApplication with RandomSessionDeleting {
      Session.create(testUser.username) must beSome.which(_.username == testSession.username)

      DB.withConnection { implicit c =>
        val maybeInsertedSession = SQL("select * from sessions where username={username} limit 1")
          .on('username -> testSession.username)
          .as(Session.session *).headOption

        maybeInsertedSession must beSome[Session]
        maybeInsertedSession.get.username mustEqual testSession.username
      }
    }

    "not be able to create session with same username" in new WithApplication with RandomSessionInsertingAndDeleting {
      Session.create(testUser.username) must beNone // Try to create the same session again
    }

    "not be able to create session with random username" in new WithApplication {
      Session.create(generateRandomText) must beNone
    }
  }

  "Session.read()" should {

    s"read test session" in new WithApplication with RandomSessionInsertingAndDeleting {
      Session.read(testSession.cookieid) must beSome(testSession)
    }

    "not be able to read session with random cookieid" in new WithApplication {
      Session.read(generateRandomText) must beNone
    }
  }

  "Session.delete()" should {

    s"delete test session" in new WithApplication with RandomSessionInserting {
      Session.delete(testSession.cookieid) mustEqual true
    }

    "not be able to delete session with random cookieid" in new WithApplication {
      Session.delete(generateRandomText) mustEqual false
    }
  }
}