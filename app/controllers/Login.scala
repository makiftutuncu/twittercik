package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{User, Session}
import play.api.Logger
import helpers.SHA512Generator

object Login extends Controller
{
  val loginForm: Form[LoginFormUser] = Form(
    mapping(
      "username" -> text(3, 24),
      "hashedpassword" -> text(6),
      "keeploggedin" -> optional(text)
    )(LoginFormUser.apply)(LoginFormUser.unapply)
  )

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
                case Some(cookieid: String) =>
                  loginFormUser.keeploggedin match {
                    case Some(keeploggedin: String) =>
                      Redirect(routes.Timeline.renderPage())
                        .withCookies(Cookie(name = "logged_user", value = cookieid, maxAge = Option(60 * 60 * 24 * 15)))
                    case _ =>
                      Redirect(routes.Timeline.renderPage()).withSession("logged_user" -> cookieid)
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

case class LoginFormUser(username: String, hashedpassword: String, keeploggedin: Option[String])