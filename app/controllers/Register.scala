package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{Session, User}
import play.api.Logger

object Register extends Controller
{
  val registerForm: Form[RegisterFormUser] = Form(
    mapping(
      "username" -> text(3, 24),
      "hashedpassword" -> text(6)
    )(RegisterFormUser.apply)(RegisterFormUser.unapply)
  )

  def renderPage = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) =>
        println(s"Register.renderPage() - User named ${session.username} is already logged in. Redirecting to timeline...")
        Redirect(routes.Timeline.renderPage())
      case None =>
        Logger.debug("Register.renderPage() - Rendering page...")
        Ok(views.html.pages.register())
    }
  }

  def submitRegisterForm = Action {
    implicit request => registerForm.bindFromRequest.fold(
      errors => {
        Logger.info("Register.submitRegisterForm() - Username or password in register form was invalid! " + errors.errorsAsJson)
        BadRequest(views.html.pages.register("username_password_invalid"))
      },
      registerFormUser => {
        Logger.debug(s"Register.submitRegisterForm() - Register form was valid for $registerFormUser.")
        User.read(registerFormUser.username) match {
          case Some(u: User) =>
            Logger.info(s"Register.submitRegisterForm() - Username ${u.username} is already registered!")
            BadRequest(views.html.pages.register("username_exists"))
          case None =>
            Logger.debug(s"Register.submitRegisterForm() - Registering ${registerFormUser.username}...")
            User.create(registerFormUser.username, registerFormUser.hashedpassword) match {
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
    )
  }
}

case class RegisterFormUser(username: String, hashedpassword: String)