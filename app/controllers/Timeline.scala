package controllers

import play.api.mvc._
import models.{User, Session, Tweetcik}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import play.api.Logger

/**
 * Timeline controller which controls everything about tweetciks
 */
object Timeline extends Controller
{
  /**
   * A form matcher for the new tweetcik form, maps the form data to a TweetcikContent object
   */
  val tweetcikForm: Form[TweetcikContent] = Form(
    mapping(
      "tweetcik" -> text(1, 140)
    )(TweetcikContent.apply)(TweetcikContent.unapply)
  )

  /**
   * Entry point of the timeline page, shows all tweetciks
   * and allows user to post new tweetciks if authorized,
   * takes user to welcome page otherwise.
   */
  def renderPage = Action {
    implicit request => Application.getSessionForRequest(request) match {
      case Some(session: Session) =>
        User.read(session.username) match {
          case Some(user: User) =>
            Logger.debug(s"Timeline.renderPage() - User named ${user.username} is logged in.")
            val numberOfTweetciks = Tweetcik.read(user.username).size
            Ok(views.html.pages.timeline(user.username, numberOfTweetciks, Tweetcik.readAll))
          case _ =>
            // This case shan't be accessed because if session is found, it guarantees that user exists too
            Logger.debug("Timeline.renderPage() - Redirecting to welcome with a new session...")
            Redirect(routes.Application.index()).withNewSession
        }
      case None =>
        Logger.debug("Timeline.renderPage() - Redirecting to welcome with a new session...")
        Redirect(routes.Application.index()).withNewSession
    }
  }

  /**
   * Action that validates new tweetcik form data and performs posting a new tweetcik,
   * takes user to welcome page if not authorized
   */
  def submitTweetcik = Action {
    implicit request => tweetcikForm.bindFromRequest.fold(
      errors => {
        Logger.info("Timeline.submitTweetcik() - Tweetcik form was invalid, empty or longer than 140 chars! " + errors.errorsAsJson)
        Redirect(routes.Timeline.renderPage())
      },
      tweetcikContent => {
        Application.getSessionForRequest(request) match {
          case Some(session: Session) =>
            Logger.debug(s"Timeline.submitTweetcik() - Posting a new tweetcik as ${tweetcikContent.content} as user named ${session.username}...")
            Tweetcik.create(session.username, tweetcikContent.content, System.currentTimeMillis()) match {
              case Some(tweetcik: Tweetcik) =>
                Logger.debug(s"Timeline.submitTweetcik() - Tweetcik posted with id ${tweetcik.id}!")
              case None =>
                Logger.info(s"Timeline.submitTweetcik() - Posting a new tweetcik failed!")
            }
            Redirect(routes.Timeline.renderPage())
          case None =>
            Logger.info(s"Timeline.submitTweetcik() - Not logged in! Redirecting to index with new session...")
            Redirect(routes.Application.index()).withNewSession
        }
      }
    )
  }

  /**
   * Action that deletes the selected tweetcik,
   * takes user to welcome page if not authorized
   *
   * @param id  Id of the tweetcik to delete
   */
  def deleteTweetcik(id: Long) = Action {
    implicit request => Application.getSessionForRequest(request) match {
      case Some(session: Session) =>
        User.read(session.username) match {
          case Some(user: User) =>
            Logger.debug(s"Timeline.deleteTweetcik() - Deleting tweetcik with id $id...")
            if(!Tweetcik.delete(id)) {
              Logger.error(s"Timeline.deleteTweetcik() - Deleting tweetcik with id $id failed!")
            }
            Redirect(routes.Timeline.renderPage())
          case _ =>
            // This case shan't be accessed because if session is found, it guarantees that user exists too
            Logger.debug("Timeline.deleteTweetcik() - Redirecting to welcome with a new session...")
            Redirect(routes.Application.index()).withNewSession
        }
      case None =>
        Logger.debug("Timeline.deleteTweetcik() - Redirecting to welcome with a new session...")
        Redirect(routes.Application.index()).withNewSession
    }
  }
}

/**
 * A model of the tweetcik input form
 *
 * @param content Content of the tweetcik
 */
case class TweetcikContent(content: String)