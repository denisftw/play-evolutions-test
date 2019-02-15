package controllers

import akka.actor.ActorSystem
import controllers.Assets.Asset
import play.api.mvc._
import services._

import play.api.data.Form
import play.api.data.Forms._

case class UserLoginData(username: String, password: String)

class Application(components: ControllerComponents, assets: Assets,
    actorSystem: ActorSystem,
    authService: AuthService,
    userAuthAction: UserAuthAction) extends AbstractController(components) {

  def index = Action {
    Ok(views.html.index())
  }

  def versioned(path: String, file: Asset) = assets.versioned(path, file)

  def restricted = userAuthAction { userAuthRequest =>
    Ok(views.html.restricted(userAuthRequest.user))
  }

  def login = Action {
    Ok(views.html.login(None))
  }

  def doLogin = Action { implicit request =>
    userDataForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.login(Some("Wrong data"))),
      userData => {
        val maybeCookie = authService.login(userData.username, userData.password)
        maybeCookie match {
          case Some(cookie) =>
            Redirect("/").withCookies(cookie)
          case None =>
            Ok(views.html.login(Some("Login failed")))
        }
      }
    )
  }

  val userDataForm = Form {
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  }
}
