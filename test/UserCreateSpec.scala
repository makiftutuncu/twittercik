import helpers.TestHelpers
import org.specs2.mutable._
import models.User
import play.api.test.WithApplication

class UserCreateSpec extends Specification with TestHelpers
{
  "UserCreate" should {

    "create a test user" in new WithApplication with UserDeleting {
      User.create(testUser.username, testUser.password) must beSome.which(_.username == testUser.username)
    }

    "not be able to create user with same name" in new WithApplication with UserInsertingAndDeleting {
      User.create(testUser.username, testUser.password) must beNone // Try to create the same user again
    }
  }
}