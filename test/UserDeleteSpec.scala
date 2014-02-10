import helpers.TestHelpers
import org.specs2.mutable._
import models.User
import play.api.test.WithApplication

class UserDeleteSpec extends Specification with TestHelpers
{
  "UserDelete" should {

    s"delete test user" in new WithApplication with UserInserting {
      User.delete(testUser.username) mustEqual true
    }

    "not be able to delete user with random name" in new WithApplication {
      User.delete(generateRandomText) mustEqual false
    }
  }
}