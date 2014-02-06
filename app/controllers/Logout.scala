package controllers

import play.api.mvc._
import models.Session
import play.api.Logger

object Logout extends Controller
{
  def logout = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) =>
        Logger.debug(s"Logout.logout() - Logging user named ${session.username} out...")
        Session.delete(session.cookieid)
        Redirect(routes.Application.index())
          .withNewSession.discardingCookies(DiscardingCookie(name = "logged_user"))
      case None =>
        Redirect(routes.Application.index())
          .withNewSession.discardingCookies(DiscardingCookie(name = "logged_user"))
    }
  }
}