import controllers.Application
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc._
import router.Routes
import play.api.routing.Router
import com.softwaremill.macwire._
import _root_.controllers.AssetsComponents
import play.api.cache.ehcache.EhCacheComponents
import scalikejdbc.config.DBs
import services.{AuthService, UserAuthAction}

import scala.concurrent.Future

class AppApplicationLoader extends ApplicationLoader {

  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with AhcWSComponents with EvolutionsComponents with DBComponents
  with HikariCPComponents with EhCacheComponents with AssetsComponents {

  private val log = Logger(this.getClass)

  override lazy val controllerComponents = wire[DefaultControllerComponents]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val applicationController = wire[Application]

  override lazy val httpFilters = Seq()

  lazy val authService: AuthService = new AuthService(defaultCacheApi.sync)

  lazy val userAuthAction = wire[UserAuthAction]

  override lazy val dynamicEvolutions = new DynamicEvolutions

  applicationLifecycle.addStopHook { () =>
    log.info("The app is about to stop")
    DBs.closeAll()
    Future.successful(Unit)
  }

  val onStart: Unit = {
    log.info("The app is about to start")
    DBs.setupAll()
    // Evolutions are checked here
    applicationEvolutions
    // The `users` must be created at this point
    val users = authService.getUsers()
    log.info(s"Obtained ${users.size} users")
  }
}
