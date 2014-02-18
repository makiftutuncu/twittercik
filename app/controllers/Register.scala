package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{FacebookUser, Session, User}
import play.api.Logger

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
  def submitRegisterForm = Action {
    implicit request => registerForm.bindFromRequest.fold(
      errors => {
        Logger.info("Register.submitRegisterForm() - Username or password in register form was invalid! " + errors.errorsAsJson)
        BadRequest(views.html.pages.register(error = "username_password_invalid"))
      },
      registerFormUser => {
        Logger.debug(s"Register.submitRegisterForm() - Register form was valid for $registerFormUser.")
        User.read(registerFormUser.username) match {
          case Some(u: User) =>
            Logger.info(s"Register.submitRegisterForm() - Username ${u.username} is already registered!")
            BadRequest(views.html.pages.register(error = "username_exists"))
          case None =>
            Logger.debug(s"Register.submitRegisterForm() - Registering ${registerFormUser.username}...")
            if(registerFormUser.userid != "" && registerFormUser.accesstoken != "" && registerFormUser.expire != "") {
              createOrUpdateFacebookUser(registerFormUser.userid, registerFormUser.username, registerFormUser.accesstoken, registerFormUser.expire) match {
                case Some(facebookUser: FacebookUser) =>
                  User.create(registerFormUser.username, registerFormUser.hashedpassword,
                    if(registerFormUser.userid != "") Option(registerFormUser.userid) else None) match {
                    case Some(user: User) =>
                      Logger.debug(s"Register.submitRegisterForm() - Creating a new session for user named ${user.username}...")
                      Session.create(user.username) match {
                        case Some(session: Session) =>
                          Redirect(routes.Timeline.renderPage())
                            .withCookies(Cookie(name = "logged_user", value = session.cookieid, maxAge = Option(60 * 60 * 24 * 15)))
                        case _ =>
                          Redirect(routes.Application.index())
                      }
                    case _ =>
                      Logger.error(s"Register.submitRegisterForm() - Cannot create a user named ${registerFormUser.username}...")
                      Redirect(routes.Application.index())
                  }
                case _ =>
                  Logger.error(s"Register.submitRegisterForm() - Cannot create or update Facebook user with id ${registerFormUser.userid}...")
                  Redirect(routes.Application.index())
              }
            }
            else {
              User.create(registerFormUser.username, registerFormUser.hashedpassword,
                if(registerFormUser.userid != "") Option(registerFormUser.userid) else None) match {
                case Some(user: User) =>
                  Logger.debug(s"Register.submitRegisterForm() - Creating a new session for user named ${user.username}...")
                  Session.create(user.username) match {
                    case Some(session: Session) =>
                      Redirect(routes.Timeline.renderPage())
                        .withCookies(Cookie(name = "logged_user", value = session.cookieid, maxAge = Option(60 * 60 * 24 * 15)))
                    case _ =>
                      Redirect(routes.Application.index())
                  }
                case _ =>
                  Logger.error(s"Register.submitRegisterForm() - Cannot create a user named ${registerFormUser.username}...")
                  Redirect(routes.Application.index())
              }
            }
        }
      }
    )
  }

  /**
   * Creates or updates a Facebook user with given information
   *
   * @param userid          Facebook user id of the user
   * @param username        Name of the user
   * @param accesstoken     Facebook access token of the user
   * @param expire          Expire time of access token after the login time in milliseconds
   *
   * @return An optional FacebookUser if successful
   */
  def createOrUpdateFacebookUser(userid: String, username: String, accesstoken: String, expire: String): Option[FacebookUser] = {
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
}

/**
 * A model of the register form
 *
 * @param username        Name of the user
 * @param hashedpassword  Hashed value of the password user entered
 * @param userid          Facebook user id of the user
 * @param accesstoken     Facebook access token of the user
 * @param expire          Expire time of access token after the login time in milliseconds
 */
case class RegisterFormUser(username: String, hashedpassword: String, userid: String, accesstoken: String, expire: String)