package controllers

import play.api._
import play.api.libs.ws._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import models.{Session, User, FacebookUser}
import scala.concurrent.Future

/**
 * Facebook Login controller which controls everything about Facebook Login flow
 */
object FacebookLogin extends Controller
{
  private def getAppId: String = Play.maybeApplication.map {
    app => app.configuration.getString("facebooklogin.appid") map {
      appid => appid
    } getOrElse {""}
  } getOrElse {""}

  private def getAppSecret: String = Play.maybeApplication.map {
    app => app.configuration.getString("facebooklogin.appsecret") map {
      appsecret => appsecret
    } getOrElse {""}
  } getOrElse {""}

  private def encodeURL(url: String): String = java.net.URLEncoder.encode(url, "UTF-8")
  private def decodeURL(url: String): String = java.net.URLDecoder.decode(url, "UTF-8")

  private def showLoginDialog[T](implicit request: Request[T]): SimpleResult = Redirect(s"https://www.facebook.com/dialog/oauth?client_id=$getAppId&redirect_uri=${encodeURL(routes.FacebookLogin.facebookLogin().absoluteURL())}")

  private def exchangeCodeForAccessToken[T](implicit request: Request[T]): Future[Response] = WS.url(s"https://graph.facebook.com/oauth/access_token?client_id=$getAppId&redirect_uri=${encodeURL(routes.FacebookLogin.facebookLogin().absoluteURL())}&client_secret=$getAppSecret&code=${getCodeFromUrl(request)}").get()
  private def getUserInfo[T](accesstoken: String)(implicit request: Request[T]): Future[Response] = WS.url(s"https://graph.facebook.com/me?access_token=$accesstoken").get()

  private def getCodeFromUrl[T](request: Request[T]): String = request.queryString.get("code") map {codes => codes(0)} getOrElse {""}
  private def getAccessTokenFromResponseBody(responseBody: String): String = responseBody.split("&")(0).split("=")(1)
  private def getExpireFromResponseBody(responseBody: String): String = responseBody.split("&")(1).split("=")(1)

  def facebookLogin = Action.async { implicit request =>
    if(!request.queryString.contains("code")) {
      // If there is no value named "code" in the URL
      Logger.debug("FacebookLogin.facebookLogin() - Showing Facebook login dialog...")
      Future.successful {
        showLoginDialog
      }
    }
    else {
      // There was a value named "code" in the URL which means user returned from Facebook login dialog with a code
      Logger.debug("FacebookLogin.facebookLogin() - Exchanging received code with access token...")
      exchangeCodeForAccessToken flatMap {
        exchangeResponse =>
          /* If successful, result will be something like this "access_token=xxx&expires=xxx"
           * Therefore, check if body contains these */
          if(exchangeResponse.body.contains("access_token=") && exchangeResponse.body.contains("expires=")) {
            val accesstoken: String = getAccessTokenFromResponseBody(exchangeResponse.body)
            val expire: String = getExpireFromResponseBody(exchangeResponse.body)
            // Now make a Facebook Graph API request to get user information
            getUserInfo(accesstoken) map {
              userInfoResponse =>
              /* Body will be a JSON containing users public profile information
               * We will take id and username */
              val userid: String = (userInfoResponse.json \ "id").as[String]
              val username: String = (userInfoResponse.json \ "username").as[String]

              // Now that we have user information, let's check if user is already registered
              FacebookUser.read(userid) match {
                case Some(existingFacebookUser: FacebookUser) =>
                  User.readWithFacebookUserId(existingFacebookUser.userid) match {
                    case Some(existingUser: User) =>
                      Logger.debug(s"FacebookLogin.facebookLogin() - Creating a new session for user named ${existingUser.username} with Facebook user id ${existingUser.fbuserid}...")
                      Session.create(existingUser.username) match {
                        case Some(session: Session) =>
                          Redirect(routes.Timeline.renderPage())
                            .withCookies(Cookie(name = "logged_user", value = session.cookieid, maxAge = Option(60 * 60 * 24 * 15)))
                        case _ =>
                          Logger.error(s"FacebookLogin.facebookLogin() - Cannot create a new session for user named ${existingUser.username} with Facebook user id ${existingUser.fbuserid}...")
                          Redirect(routes.Register.renderPage("", "", "", ""))
                      }
                    case _ =>
                      Logger.error(s"FacebookLogin.facebookLogin() - Facebook user is found but corresponding user is not found for user named ${existingFacebookUser.username} with Facebook user id ${existingFacebookUser.userid}...")
                      Redirect(routes.Register.renderPage("", "", "", ""))
                  }
                case _ =>
                  // User is not registered, show register page with Facebook information
                  Redirect(routes.Register.renderPage(userid, username, accesstoken, expire))
              }
            } recover {
              case e: Exception =>
                Logger.error(s"FacebookLogin.facebookLogin() - There was an error retrieving the user info! ${e.getMessage}")
                BadRequest(views.html.pages.register(error = "facebook_error"))
            }
          }
          else {
            Logger.error("FacebookLogin.facebookLogin() - There was an error retrieving the access token!")
            Future.successful(BadRequest(views.html.pages.register(error = "facebook_error")))
          }
      } recover {
        case e: Exception =>
          Logger.error(s"FacebookLogin.facebookLogin() - There was an error retrieving the access token! ${e.getMessage}")
          BadRequest(views.html.pages.register(error = "facebook_error"))
      }
    }
  }
}