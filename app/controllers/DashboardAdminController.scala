package controllers

import com.google.gson.GsonBuilder
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import config.DashboardConfig
import model.DashboardModel._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.RequestUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class DashboardAdminController @Inject()(val requestUtils: RequestUtils,
                                          val controllerComponents: ControllerComponents) extends BaseController {

  // application status page

  def status = Action {
    val renderOpts: ConfigRenderOptions = ConfigRenderOptions.defaults().setOriginComments(false).setComments(false).setJson(false);
    val applicationProperties: Config = ConfigFactory.load
    val applicationPropertiesAsString: String = applicationProperties.root().render(renderOpts)
    Ok(
      applicationPropertiesAsString
    )
  }

  // config CRUD operations

  def viewCurrentConfig = Action {
    Ok(
      views.html.admin.dashboardConfig(DashboardConfig.get())
    )
  }

  def updateCurrentConfig() = Action { implicit request =>
    val configForm = Form(
        "configFile" -> nonEmptyText
    )
    val (configFile) = configForm.bindFromRequest()
    DashboardConfig.updateConfig(configFile.get)
    Redirect(routes.DashboardAdminController.viewCurrentConfig)
  }

  def viewConfigHistory = Action {
    Ok(
      views.html.admin.dashboardConfigHistory(DashboardConfig.getHistory)
    )
  }

  def deleteConfig(configName: String) = Action {
    DashboardConfig.deleteHistoryConfig(Option(configName))
    Redirect(routes.DashboardAdminController.viewConfigHistory)
  }

  def restoreConfig(configName: String) = Action {
    DashboardConfig.restoreConfig(Option(configName))
    Redirect(routes.DashboardAdminController.viewConfigHistory)
  }

  def viewConfig(configName: String) = Action {
    Ok(
      Json.parse(DashboardConfig.viewConfig(configName))
    )
  }

  // config builder
  def updateConfigBuilder() = Action.async { implicit request =>
    val configBuilderForm = Form(
      tuple(
        "statusPageVersionRegex" -> text,
        "statusPageUrl" -> text
      )
    )

    val (statusPageVersionRegex, statusPageUrl) = configBuilderForm.bindFromRequest().get

    if (Option(statusPageUrl).getOrElse("").isEmpty || Option(statusPageVersionRegex).getOrElse("").isEmpty) {
      Future.successful(
        Ok(views.html.admin.dashboardConfigBuilder(Option.empty, Option.empty, Option("Status page url and version regex required!")))
      )
    } else {
      val env = Environment("test", statusPageUrl)
      val application = Application(name="TestApp", statusPageVersionRegex=statusPageVersionRegex, hostRegex="", environments=Array(env), links=Array(Link("Endpoints", "/service/endpoints")))
      val dashboard = Dashboard(applications = Array(application))
      val dashboardJsonConfig: String = new GsonBuilder().setPrettyPrinting().create().toJson(dashboard)
      val applicationStatusFuture: Future[ApplicationStatus] = requestUtils.buildApplicationStatusPage(application, env)
      applicationStatusFuture.map { applicationStatus =>
        Ok(
          views.html.admin.dashboardConfigBuilder(Option(dashboardJsonConfig), Option(applicationStatus), Option.empty)
        )
      }
    }

  }

  def viewConfigBuilder = Action {
    Ok(
      views.html.admin.dashboardConfigBuilder()
    )
  }
}