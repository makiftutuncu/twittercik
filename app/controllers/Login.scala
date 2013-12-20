package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.{User, Session}

object Login extends Controller {

  val loginForm: Form[User] = Form(
    mapping(
      "username" -> text(3, 24),
      "password" -> text(6)
    )(User.apply)(User.unapply)
  )

  def index = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) => {
        println("Login.index - Redirecting to timeline with " + session + "...")
        Redirect(routes.Timeline.index())
      }
      case None => {
        println("Login.index - Redirecting to login...")
        Unauthorized(views.html.login())
      }
    }
  }

  def submit = Action {
    implicit request => loginForm.bindFromRequest.fold(
      errors => {
        println("Login.submit - Username or password in login form was invalid!")
        BadRequest(views.html.login("username_password_invalid"))
      },
      user => {
        println("Login.submit - Login form was valid for " + user)
        User.getUser(user.username) match {
          case Some(u: User) => {
            if(user.password.hashCode().toString == u.password) {
              println("Login.submit - Username and password matches")
              val uuid: String = Session.create(user.username)
              Redirect(routes.Timeline.index()).withSession("logged_user" -> uuid)
            }
            else
            {
              println("Login.submit - Username and password doesn't match!")
              BadRequest(views.html.login("username_password_mismatch"))
            }
          }
          case None => {
            println("Login.submit - Username and password doesn't match!")
            BadRequest(views.html.login("username_password_mismatch"))
          }
        }
      }
    )
  }

  def logout = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) => {
        Session.delete(session.uuid)
        Ok(views.html.welcome()).withNewSession
      }
      case None => Ok(views.html.welcome()).withNewSession
    }
  }
}