package controllers

import play.api.mvc._
import models.Session

object Application extends Controller {

  def isAuthorized[T](request: Request[T]): Option[Session] =
    request.session.get("logged_user").map {
      uuid => {
        println("Application.authorize - Session cookie found: " + uuid)
        Session.getSession(uuid) match {
          case x @ Some(s: Session) => {
            println("Application.authorize - Authorization successful as " + s)
            x
          }
          case None => {
            println("Application.authorize - Authorization failed!")
            None
          }
        }
      }
    }.getOrElse {
      println("Application.authorize - Session cookie not found.")
      None
    }


  def index = Action {
    implicit request => isAuthorized(request) match {
      case Some(session: Session) => {
        println("Application.index - Redirecting to timeline with " + session + "...")
        Redirect(routes.Timeline.index())
      }
      case None => {
        println("Application.index - Redirecting to welcome with a new session...")
        Unauthorized(views.html.welcome()).withNewSession
      }
    }
  }
}