import helpers.SHA512Generator
import org.specs2.mutable._
import models.{User, Session}
import play.api.test.WithApplication

class SessionSpec extends Specification
{
  val testUserName = "test"
  val testUserPassword = SHA512Generator.generate("123456")
  var testUserCookieId: String = ""
  val crapCookieId = "foobar"
  val userThatDoesntExist = "foobar"

  "Session" should {

    "create create a test user and then create a session for test user" in new WithApplication {
      if(User.create(testUserName, testUserPassword)) {
        Session.create(testUserName) match {
          case Some(cookieid: String) =>
            testUserCookieId = cookieid
            success
          case _ => failure("Cannot create session for test user!")
        }
      }
      else failure("Cannot create test user!")
    }

    "read for a test user" in new WithApplication {
      Session.read(testUserCookieId) must beSome(Session(testUserCookieId, testUserName))
    }

    "delete test user and then delete session for test user" in new WithApplication {
      if(User.delete(testUserName))
        Session.delete(testUserCookieId) mustEqual true
      else failure("Cannot delete test user!")
    }

    s"not be able to create for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Session.create(userThatDoesntExist) must beNone
    }

    s"not be able to read for crap cookie id $crapCookieId" in new WithApplication {
      Session.read(crapCookieId) must beNone
    }

    s"not be able to delete for a crap cookie id $crapCookieId" in new WithApplication {
      Session.delete(crapCookieId) mustEqual false
    }
  }
}