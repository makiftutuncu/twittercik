package controllers

import play.api.mvc._
import models.{User, Session, Tweetcik}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import play.api.Logger

case class TweetcikContent(content: String)

object Timeline extends Controller {

  val tweetcikForm: Form[TweetcikContent] = Form(
    mapping(
      "tweetcik" -> text(1, 140)
    )(TweetcikContent.apply)(TweetcikContent.unapply)
  )

  def renderPage = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) =>
        User.getByUsername(session.username) match {
          case Some(user: User) =>
            Logger.debug(s"Timeline.renderPage() - User named ${user.username} is logged in.")
            val numberOfTweetciks = Tweetcik.getTweetciksByUsername(user.username).size
            Ok(views.html.pages.timeline(user.username, numberOfTweetciks, Tweetcik.getAllTweetciks()))

          case _ =>
            Logger.debug("Timeline.renderPage() - Redirecting to welcome with a new session...")
            Redirect(routes.Application.index()).withNewSession
        }

      case None =>
        Logger.debug("Timeline.renderPage() - Redirecting to welcome with a new session...")
        Redirect(routes.Application.index()).withNewSession
    }
  }

  def submitTweetcik = Action {
    implicit request => tweetcikForm.bindFromRequest.fold(
      errors => {
        Logger.info("Timeline.submitTweetcik() - Tweetcik form was invalid, empty or longer than 140 chars! " + errors.errorsAsJson)
        Redirect(routes.Timeline.renderPage())
      },
      tweetcikContent => {
        Application.isAuthorized(request) match {
          case Some(session: Session) => {
            Logger.debug(s"Timeline.submitTweetcik() - Posting a new tweetcik as ${tweetcikContent.content} as user with id ${session.username}...")
            Tweetcik.create(session.username, tweetcikContent.content, System.currentTimeMillis())
            Redirect(routes.Timeline.renderPage())
          }
          case None =>
            Logger.info(s"Timeline.submitTweetcik() - Not logged in! Redirecting to index with new session...")
            Redirect(routes.Application.index()).withNewSession
        }
      }
    )
  }

  def removeTweetcik(id: Long) = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) =>
        User.getByUsername(session.username) match {
          case Some(user: User) =>
            Logger.debug(s"Timeline.removeTweetcik() - Removing tweetcik with id ${id}...")
            Tweetcik.remove(id)
            Redirect(routes.Timeline.renderPage())

          case _ =>
            Logger.debug("Timeline.removeTweetcik() - Redirecting to welcome with a new session...")
            Redirect(routes.Application.index()).withNewSession
        }

      case None =>
        Logger.debug("Timeline.removeTweetcik() - Redirecting to welcome with a new session...")
        Redirect(routes.Application.index()).withNewSession
    }
  }
}