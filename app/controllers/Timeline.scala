package controllers

import play.api.mvc._
import models.{TweetcikContent, Session, Tweetcik}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some

object Timeline extends Controller {

  val tweetcikForm: Form[TweetcikContent] = Form(
    mapping(
      "tweetcik" -> text(1, 140)
    )(TweetcikContent.apply)(TweetcikContent.unapply)
  )

  def index = Action {
    implicit request => Application.isAuthorized(request) match {
      case Some(session: Session) => {
        println("Login.index - Showing to timeline with " + session + "...")
        Ok(views.html.timeline(session.username, Tweetcik.getAllTweetciks()))
      }
      case None => {
        println("Login.index - Redirecting to welcome...")
        Unauthorized(views.html.welcome()).withNewSession
      }
    }
  }

  def submit = Action {
    implicit request => tweetcikForm.bindFromRequest.fold(
      errors => {
        println("Login.submit - Tweetcik form was invalid, empty or longer than 140 chars!")
        Redirect(routes.Timeline.index())
      },
      tweetcikContent => {
        Application.isAuthorized(request) match {
          case Some(session: Session) => {
            Tweetcik.create(session.username, tweetcikContent.content, System.currentTimeMillis())
            Redirect(routes.Timeline.index())
          }
          case None => Unauthorized(views.html.welcome()).withNewSession
        }
      }
    )
  }
}