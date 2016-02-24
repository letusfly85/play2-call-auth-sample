package controllers

import dispatch.{url, Http}
import dispatch._, Defaults._
import play.api.mvc.{Action, Controller}

import argonaut._, Argonaut._


class AuthController extends Controller {

  case class UserAuthToken(tokenType: String, accessToken: String, expiresIn: Int, refreshToken: String)

  implicit def UserAuthTokenCodecJson =
    casecodec4(UserAuthToken.apply, UserAuthToken.unapply)("token_type", "access_token", "expires_in", "refresh_token")

  def accessToken = Action {implicit request =>
    val authUrl = System.getProperty("auth.url")

    val host = url(authUrl)
    val request = host / "oauth" / "access_token"

    //curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_id" -d "client_secret=bob_client_secret" -d "grant_type=client_credentials"
    val requestAsJson = request.POST.setContentType("application/json", "UTF-8")
    val postWithBody  = requestAsJson << """{"client_id": "bob_client_id", "client_secret": "bob_client_secret", "grant_type": "client_credentials"}"""

    val result = Http(postWithBody OK as.String).apply()

    val option: Option[UserAuthToken] =
      Parse.decodeOption[UserAuthToken](result)

    option match {
      case Some(opt) =>
        println(opt)

        //curl --dump-header - -H "Authorization: Bearer ${access_token}" http://localhost:9000/resources
        val resourceRequest = host / "resources"
        val resourceRequestWithHeader = resourceRequest.setHeader("Authorization", s"${opt.tokenType} ${opt.accessToken}")

        val resourceResult = Http(resourceRequestWithHeader OK as.String).apply()
        println(resourceResult)

      case None => println("error....")
    }
    Ok("ok")
  }

}
