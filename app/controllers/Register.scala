package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{FacebookUser, Session, User}
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Register controller which controls everything about registering a new user to the system
 */
object Register extends Controller
{
  /**
   * A form matcher for the user register form, maps the form data to a RegisterFormUser object
   */
  val registerForm: Form[RegisterFormUser] = Form(
    mapping(
      "username" -> text(3, 24),
      "hashedpassword" -> text(6),
      "userid" -> text,
      "fbusername" -> text,
      "accesstoken" -> text,
      "expire" -> text
    )(RegisterFormUser.apply)(RegisterFormUser.unapply)
  )

  /**
   * Entry point of the register page, takes user to timeline if authorized,
   * shows register page otherwise.
   */
  def renderPage(userid: String, username: String, accesstoken: String, expire: String) = Action {
    implicit request => Application.getSessionForRequest(request) match {
      case Some(session: Session) =>
        println(s"Register.renderPageWithFacebookData() - User named ${session.username} is already logged in. Redirecting to timeline...")
        Redirect(routes.Timeline.renderPage())
      case None =>
        Logger.debug("Register.renderPageWithFacebookData() - Rendering page...")
        Ok(views.html.pages.register("", userid, username, accesstoken, expire))
    }
  }

  /**
   * Action that validates register data and performs register operation,
   * takes user to welcome page if not authorized
   */
  def submitRegisterForm = Action.async {
    implicit request => registerForm.bindFromRequest.fold(
      errors =>
        giveFormError("Register.submitRegisterForm() - Username or password in register form was invalid! " + errors.errorsAsJson,
          "username_password_invalid"),
      registerFormUser => {
        Logger.debug(s"Register.submitRegisterForm() - Register form was valid for $registerFormUser.")
        if(!userExists(registerFormUser.username)) {
          if(hasFacebookData(registerFormUser.userid, registerFormUser.accesstoken, registerFormUser.expire)) {
            isFacebookDataValid(registerFormUser.userid, registerFormUser.fbusername, registerFormUser.accesstoken) flatMap {
              isValid: Boolean =>
                if(isValid) {
                  Logger.debug(s"Register.submitRegisterForm() - Registering ${registerFormUser.username} as Facebook user with id ${registerFormUser.userid}...")
                  createOrUpdateFacebookUser(registerFormUser.userid, registerFormUser.fbusername, registerFormUser.accesstoken, registerFormUser.expire) match {
                    case Some(facebookUser: FacebookUser) =>
                      createAndLogUserIn(registerFormUser.username, registerFormUser.hashedpassword, Option(registerFormUser.userid))
                    case _ =>
                      giveError(s"Register.submitRegisterForm() - Cannot create user named ${registerFormUser.username}...")
                  }
                }
                else giveError("Register.submitRegisterForm() - Given Facebook data is not valid, possible attack!")
            } recoverWith {
              case e: Exception =>
                giveError(s"Register.submitRegisterForm() - There was an error retrieving the user info! ${e.getMessage}")
            }
          } else {
            Logger.debug(s"Register.submitRegisterForm() - Registering ${registerFormUser.username}...")
            createAndLogUserIn(registerFormUser.username, registerFormUser.hashedpassword, None)
          }
        } else {
          giveFormError(s"Register.submitRegisterForm() - Username ${registerFormUser.username} is already registered!",
            "username_exists")
        }
      }
    )
  }

  /**
   * Generates a result for a form error, logs it and returns to register page with a bad request
   *
   * @param logMsg        Message to write to log
   * @param formErrorMsg  Message code for identifying error message in register page
   *
   * @return  A SimpleResult wrapped in a Future
   */
  private def giveFormError(logMsg: String, formErrorMsg: String): Future[SimpleResult] = {
    Logger.error(logMsg)
    Future.successful(BadRequest(views.html.pages.register(error = formErrorMsg)))
  }

  /**
   * Generates a result for an error, logs it and returns to welcome page with redirect
   *
   * @param logMsg  Message to write to log
   *
   * @return  A SimpleResult wrapped in a Future
   */
  private def giveError(logMsg: String): Future[SimpleResult] = {
    Logger.error(logMsg)
    Future.successful(Redirect(routes.Application.index()))
  }

  /**
   * Checks whether a user exist in database or not
   *
   * @param username  Name of the user
   *
   * @return  true if user exists in database, false otherwise
   */
  private def userExists(username: String): Boolean = User.read(username) map { user: User => true } getOrElse { false }

  /**
   * Checks whether register form has Facebook data or not (user is logging in with Facebook or not)
   *
   * @param userid        Facebook user id of the user
   * @param accesstoken   Facebook access token of the user
   * @param expire        Expire time of access token after the login time in milliseconds
   *
   * @return  true if register form has Facebook data, false otherwise
   */
  private def hasFacebookData(userid: String, accesstoken: String, expire: String): Boolean = userid != "" && accesstoken != "" && expire != ""

  /**
   * Validates given Facebook data is valid or not
   *
   * @param userid          Facebook user id of the user
   * @param username        Facebook username of the user
   * @param accesstoken     Facebook access token of the user
   *
   * @return  true if data is valid
   */
  private def isFacebookDataValid[T](userid: String, username: String, accesstoken: String)(implicit request: Request[T]): Future[Boolean] =
    FacebookLogin.getUserInfo(accesstoken) map {
      userInfoResponse =>
        val receivedUserid: String = (userInfoResponse.json \ "id").as[String]
        val receivedUsername: String = (userInfoResponse.json \ "username").as[String]
        receivedUserid == userid && receivedUsername == username
    }

  /**
   * Creates or updates a Facebook user with given information
   *
   * @param userid          Facebook user id of the user
   * @param username        Name of the user
   * @param accesstoken     Facebook access token of the user
   * @param expire          Expire time of access token after the login time in milliseconds
   *
   * @return  An optional FacebookUser if successful
   */
  private def createOrUpdateFacebookUser(userid: String, username: String, accesstoken: String, expire: String): Option[FacebookUser] = {
    try {
      // We have everything, let's update the database for this Facebook user!
      FacebookUser.read(userid) match {
        case Some(existingFacebookUser: FacebookUser) =>
          // This Facebook user already exists. Let's update access token, login time and expire time.
          FacebookUser.update(userid, accesstoken, expire.toLong) match {
            case Some(updatedFacebookUser: FacebookUser) =>
              // Successfully updated. Now let's move to register page with Facebook register information.
              Logger.debug(s"Register.createOrUpdateFacebookUser() - Facebook user is updated $updatedFacebookUser")
              Option(updatedFacebookUser)
            case _ =>
              Logger.error("Register.createOrUpdateFacebookUser() - There was an error updating Facebook user!")
              None
          }
        case _ =>
          // This Facebook user is new. Let's add him/her to the database then.
          FacebookUser.create(userid, username, accesstoken, expire.toLong) match {
            case Some(createdFacebookUser: FacebookUser) =>
              // Successfully added. Now let's move to register page with Facebook register information.
              Logger.debug(s"Register.createOrUpdateFacebookUser() - Facebook user is created $createdFacebookUser")
              Option(createdFacebookUser)
            case _ =>
              Logger.error("Register.createOrUpdateFacebookUser() - There was an error creating Facebook user!")
              None
          }
      }
    }
    catch {
      case e: Exception =>
        Logger.error(s"Register.createOrUpdateFacebookUser() - ${e.getMessage}")
        None
    }
  }

  /**
   * Creates a user for given information in the database, logs the user in and returns to timeline page with redirect
   *
   * @param username        Name of the user
   * @param hashedpassword  Hashed value of the password user entered
   * @param userid          Facebook user id of the user
   *
   * @return  A SimpleResult wrapped in a Future
   */
  private def createAndLogUserIn(username: String, hashedpassword: String, userid: Option[String] = None): Future[SimpleResult] = {
    User.create(username, hashedpassword, userid) map {
      user: User =>
        Logger.debug(s"Register.submitRegisterForm() - Creating a new session for user named ${user.username}...")
        Session.create(user.username) map {
          session: Session =>
            Future.successful(Redirect(routes.Timeline.renderPage()).withCookies(Cookie(name = "logged_user", value = session.cookieid, maxAge = Option(60 * 60 * 24 * 15))))
        } getOrElse {
          giveError(s"Register.submitRegisterForm() - Cannot create session for user named ${user.username}...")
        }
    } getOrElse {
      giveError(s"Register.submitRegisterForm() - Cannot create user named $username...")
    }
  }
}

/**
 * A model of the register form
 *
 * @param username        Name of the user
 * @param hashedpassword  Hashed value of the password user entered
 * @param userid          Facebook user id of the user
 * @param fbusername      Facebook user name of the user
 * @param accesstoken     Facebook access token of the user
 * @param expire          Expire time of access token after the login time in milliseconds
 */
case class RegisterFormUser(username: String, hashedpassword: String, userid: String, fbusername: String, accesstoken: String, expire: String)