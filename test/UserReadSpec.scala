import helpers.TestHelpers
import org.specs2.mutable._
import models.User
import play.api.test.WithApplication

class UserReadSpec extends Specification with TestHelpers
{
  "UserRead" should {

    s"read test user" in new WithApplication with UserInsertingAndDeleting {
      User.read(testUser.username) must beSome(testUser)
    }

    "not be able to read user with random name" in new WithApplication {
      User.read(generateRandomText) must beNone
    }
  }
}