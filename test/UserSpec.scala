import anorm._
import helpers.TestHelpers._
import org.specs2.mutable._
import models.User
import play.api.db.DB
import play.api.test.WithApplication

/**
 * Integration tests and specifications for User
 */
class UserSpec extends Specification
{
  "User.create()" should {

    "create a test user" in new WithApplication with RandomUserDeleting {
      User.create(testUser.username, testUser.password) must beSome.which(_.username == testUser.username)

      DB.withConnection { implicit c =>
        val maybeUser = SQL("select * from users where username={username} limit 1")
          .on('username -> testUser.username).as(User.user *).headOption

        maybeUser must beSome[User]
        maybeUser.get.username mustEqual testUser.username
      }
    }

    "not be able to create user with same name" in new WithApplication with RandomUserInsertingAndDeleting {
      User.create(testUser.username, testUser.password) must beNone // Try to create the same user again
    }
  }

  "User.read()" should {

    s"read test user" in new WithApplication with RandomUserInsertingAndDeleting {
      User.read(testUser.username) must beSome(testUser)
    }

    "not be able to read user with random name" in new WithApplication {
      User.read(generateRandomText) must beNone
    }
  }

  "User.delete()" should {

    s"delete test user" in new WithApplication with RandomUserInserting {
      User.delete(testUser.username) mustEqual true
    }

    "not be able to delete user with random name" in new WithApplication {
      User.delete(generateRandomText) mustEqual false
    }
  }
}