package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{User, Session}
import play.api.Logger
import helpers.SHA512Generator

/**
 * Login controller which controls everything about logging a user into the system
 */
object Login extends Controller
{
  /**
   * A form matcher for the user log in form, maps the form data to a LoginFormUser object
   */
  val loginForm: Form[LoginFormUser] = Form(
    mapping(
      "username" -> text(3, 24),
      "hashedpassword" -> text(6),
      "keeploggedin" -> optional(text)
    )(LoginFormUser.apply)(LoginFormUser.unapply)
  )

  /**
   * Entry point of the login page, takes user to timeline if authorized,
   * shows login page otherwise.
   */
  def renderPage = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) =>
        Logger.debug(s"Login.renderPage() - User named ${session.username} is already logged in. Redirecting to timeline...")
        Redirect(routes.Timeline.renderPage())
      case None =>
        Logger.debug("Login.renderPage() - Rendering page...")
        Ok(views.html.pages.login())
    }
  }

  /**
   * Action that validates login data and performs login operation,
   * takes user to welcome page if not authorized
   */
  def submitLoginForm = Action {
    implicit request => loginForm.bindFromRequest.fold(
      errors => {
        Logger.info("Login.submitLoginForm() - Username or password in login form was invalid! " + errors.errorsAsJson)
        BadRequest(views.html.pages.login("username_password_invalid"))
      },
      loginFormUser => {
        Logger.debug(s"Login.submitLoginForm() - Login form was valid for $loginFormUser.")
        User.read(loginFormUser.username) match {
          case Some(u: User) =>
            val saltedPassword = loginFormUser.hashedpassword + u.salt
            val calculatedPassword = SHA512Generator.generate(saltedPassword)
            if(calculatedPassword == u.password) {
              Logger.debug(s"Login.submitLoginForm() - Username and password matches for ${loginFormUser.username}.")
              Session.create(loginFormUser.username) match {
                case Some(session: Session) =>
                  loginFormUser.keeploggedin match {
                    case Some(keeploggedin: String) =>
                      Redirect(routes.Timeline.renderPage())
                        .withCookies(Cookie(name = "logged_user", value = session.cookieid, maxAge = Option(60 * 60 * 24 * 15)))
                    case _ =>
                      Redirect(routes.Timeline.renderPage()).withSession("logged_user" -> session.cookieid)
                  }
                case _ =>
                  Redirect(routes.Application.index())
              }
            }
            else {
              Logger.info("Login.submitLoginForm() - Username and password doesn't match!")
              BadRequest(views.html.pages.login("username_password_mismatch"))
            }
          case None =>
            Logger.info("Login.submitLoginForm() - Username and password doesn't match!")
            BadRequest(views.html.pages.login("username_password_mismatch"))
        }
      }
    )
  }
}

/**
 * A model of the login form
 *
 * @param username        Name of the user
 * @param hashedpassword  Hashed value of the password user entered
 * @param keeploggedin    Flag to keep user logged in between sessions
 *                        (Value will be "on" if user checked "keep logged in" option)
 */
case class LoginFormUser(username: String, hashedpassword: String, keeploggedin: Option[String])