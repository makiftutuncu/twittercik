import helpers.SHA512Generator
import org.specs2.mutable._
import models.User
import play.api.test.WithApplication

class UserSpec extends Specification
{
  val testUserName = "test"
  val testUserPassword = SHA512Generator.generate("123456")
  val userThatDoesntExist = "foobar"

  "User" should {

    "create for test user" in new WithApplication {
      User.create(testUserName, testUserPassword) mustEqual true
    }

    s"read for user named $testUserName" in new WithApplication {
      User.read(testUserName) must not beNone
    }

    s"delete for user named $testUserName" in new WithApplication {
      User.delete(testUserName) mustEqual true
    }

    s"not be able to read for user named $userThatDoesntExist" in new WithApplication {
      User.read(userThatDoesntExist) must beNone
    }

    s"not be able to delete for user named $userThatDoesntExist" in new WithApplication {
      User.delete(userThatDoesntExist) mustEqual false
    }
  }
}