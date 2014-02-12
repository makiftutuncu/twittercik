package controllers

import play.api.mvc._
import models.Session
import play.api.Logger

/**
 * Logout controller which controls everything about logging a user out of the system
 */
object Logout extends Controller
{
  /**
   * Action that performs logout operation for the user defined by the session
   * and takes user to welcome page
   */
  def logout = Action {
    implicit request => Application.getSessionForRequest(request) match {
      case Some(session: Session) =>
        Logger.debug(s"Logout.logout() - Logging user named ${session.username} out...")
        if(!Session.delete(session.cookieid)) {
          Logger.error(s"Logout.logout() - Logging user named ${session.username} failed!")
        }
        Redirect(routes.Application.index())
          .withNewSession.discardingCookies(DiscardingCookie(name = "logged_user"))
      case None =>
        Redirect(routes.Application.index())
          .withNewSession.discardingCookies(DiscardingCookie(name = "logged_user"))
    }
  }
}