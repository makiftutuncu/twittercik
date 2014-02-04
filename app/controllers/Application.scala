package controllers

import play.api.mvc._
import models.Session
import play.api.Logger

object Application extends Controller {

  def isAuthorized[T](request: Request[T]): Option[Session] =
    // Look for cookie first
    request.cookies.get("logged_user") match {
      case Some(cookie: Cookie) =>
        Logger.debug(s"Application.authorize() - Cookie found with cookieid ${cookie.value}")
        Session.getByCookieId(cookie.value) match {
          case optionSession @ Some(s: Session) => {
            Logger.debug(s"Application.authorize() - Authorization successful as user named ${s.username}.")
            optionSession
          }
          case None => {
            Logger.error(s"Application.authorize() - Authorization failed for cookie with id ${cookie.value}! Possible attack!")
            None
          }
        }

      case None =>
        // Look for session cookie
        request.session.get("logged_user") match {
          case Some(cookieid: String) =>
            Logger.debug(s"Application.authorize() - Session cookie found with cookieid ${cookieid}")
            Session.getByCookieId(cookieid) match {
              case optionSession @ Some(s: Session) => {
                Logger.debug(s"Application.authorize() - Authorization successful as user named ${s.username}.")
                optionSession
              }
              case None => {
                Logger.error(s"Application.authorize() - Authorization failed for session cookie with id ${cookieid}! Possible attack!")
                None
              }
            }

          case None =>
            Logger.debug("Application.authorize() - No session cookie found. Not logged in.")
            None
        }
    }


  def index = Action {
    implicit request => isAuthorized(request) match {
      case Some(session: Session) => {
        println(s"Application.index() - User named ${session.username} is already logged in. Redirecting to timeline...")
        Redirect(routes.Timeline.renderPage())
      }
      case None => {
        Logger.debug("Application.index() - Rendering welcome page...")
        Ok(views.html.pages.welcome())
      }
    }
  }
}