package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{Session, User}

object Register extends Controller {

  val registerForm: Form[User] = Form(
    mapping(
      "username" -> text(3, 24),
      "password" -> text(6)
    )(User.apply)(User.unapply)
  )

  def index = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) => {
        println("Register.index - Redirecting to timeline with " + session + "...")
        Redirect(routes.Timeline.index())
      }
      case None => {
        println("Register.index - Redirecting to register...")
        Unauthorized(views.html.register())
      }
    }
  }

  def submit = Action {
    implicit request => registerForm.bindFromRequest.fold(
      errors => {
        println("Register.submit - Username or password in register form was invalid!")
        BadRequest(views.html.register("username_password_invalid"))
      },
      user => {
        println("Register.submit - Register form was valid for " + user)
        User.getUser(user.username) match {
          case Some(u: User) => {
            println("Register.submit - Username already exists!")
            BadRequest(views.html.register("username_exists"))
          }
          case None => {
            User.create(user.username, "" + user.password.hashCode())
            val uuid: String = Session.create(user.username)
            Redirect(routes.Timeline.index()).withSession("logged_user" -> uuid)
          }
        }
      }
    )
  }
}