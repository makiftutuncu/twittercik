import anorm._
import helpers.SHA512Generator
import org.specs2.mutable._
import models.Session
import play.api.db.DB
import play.api.test.{FakeApplication, WithApplication}
import scala.Some

class SessionSpec extends Specification
{
  "Session" should {

    step(before)

    "create session for test user" in new WithApplication {
      Session.create(testUserName) match {
        case Some(cookieid: String) =>
          testUserCookieId = cookieid
          success
        case _ => failure("Cannot create session for test user!")
      }
    }

    "read session of test user" in new WithApplication {
      Session.read(testUserCookieId) must beSome(Session(testUserCookieId, testUserName))
    }

    "delete session of test user" in new WithApplication {
      Session.delete(testUserCookieId) mustEqual true
    }

    step(after)

    s"not be able to create for a user $userThatDoesntExist that doesn't exist" in new WithApplication {
      Session.create(userThatDoesntExist) must beNone
    }

    s"not be able to read with crap cookie id $crapCookieId" in new WithApplication {
      Session.read(crapCookieId) must beNone
    }

    s"not be able to delete with crap cookie id $crapCookieId" in new WithApplication {
      Session.delete(crapCookieId) mustEqual false
    }
  }

  implicit val app = FakeApplication()
  val testUserName = "test"
  val testUserPassword = SHA512Generator.generate("123456")
  var testUserCookieId: String = ""
  val crapCookieId = "foobar"
  val userThatDoesntExist = "foobar"

  def before = {
    DB.withConnection { implicit c =>
      try {
        val salt = SHA512Generator.generate(testUserName + System.currentTimeMillis())
        val saltedPassword = SHA512Generator.generate(testUserPassword + salt)
        SQL("insert into users (username, password, salt) values ({username}, {password}, {salt})")
          .on('username -> testUserName, 'password -> saltedPassword, 'salt -> salt)
          .executeUpdate()
      }
      catch {
        case e: Exception => failure(e.getMessage)
      }
    }
  }

  def after = {
    try {
      DB.withConnection { implicit c =>
        SQL("delete from users where username = {username}").on('username -> testUserName)
          .executeUpdate()
      }
    }
    catch {
      case e: Exception => failure(e.getMessage)
    }
  }
}