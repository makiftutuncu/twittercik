package controllers

import play.api.mvc._
import models.Session
import play.api.Logger

/**
 * Main controller and the entry point of the application
 */
object Application extends Controller
{
  /**
   * A general purpose authorization check for each request,
   * it checks cookies for a cookie id matching a user.
   *
   * @param request Request of the action
   * @tparam T      Type of the request
   *
   * @return  An optional session containing cookie id and username if authorization is successful
   */
  def getSessionForRequest[T](request: Request[T]): Option[Session] = {
    // Look for cookie first
    request.cookies.get("logged_user") match {
      case Some(cookie: Cookie) =>
        Logger.debug(s"Application.authorize() - Cookie found with cookieid ${cookie.value}")
        Session.read(cookie.value) match {
          case optionSession @ Some(s: Session) =>
            Logger.debug(s"Application.authorize() - Authorization successful as user named ${s.username}.")
            optionSession
          case None =>
            Logger.error(s"Application.authorize() - Authorization failed for cookie with id ${cookie.value}! Possible attack!")
            None
        }
      case None =>
        // Look for session cookie
        request.session.get("logged_user") match {
          case Some(cookieid: String) =>
            Logger.debug(s"Application.authorize() - Session cookie found with cookieid $cookieid")
            Session.read(cookieid) match {
              case optionSession @ Some(s: Session) =>
                Logger.debug(s"Application.authorize() - Authorization successful as user named ${s.username}.")
                optionSession
              case None =>
                Logger.error(s"Application.authorize() - Authorization failed for session cookie with id $cookieid! Possible attack!")
                None
            }
          case None =>
            Logger.debug("Application.authorize() - No credentials found. Not logged in.")
            None
        }
    }
  }

  /**
   * Entry point of the application, takes user to timeline if authorized,
   * shows welcome page otherwise.
   */
  def index = Action {
    implicit request => getSessionForRequest(request) match {
      case Some(session: Session) =>
        println(s"Application.index() - User named ${session.username} is already logged in. Redirecting to timeline...")
        Redirect(routes.Timeline.renderPage())
      case None =>
        Logger.debug("Application.index() - Rendering welcome page...")
        Ok(views.html.pages.welcome())
    }
  }
}