package controllers

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import config.DashboardConfig
import controllers.TestStatusPages._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._


object AdminEndpoints extends Controller {

  def statusPage = Action {
    val renderOpts = ConfigRenderOptions.defaults().setOriginComments(false).setComments(false).setJson(false);
    val applicationProperties = ConfigFactory.load
    val applicationPropertiesAsString: String = applicationProperties.root().render(renderOpts)
    Ok(
      applicationPropertiesAsString
    )
  }

  def viewCurrentConfig = Action {
    Ok(
      views.html.admin.dashboardConfig(DashboardConfig.get())
    )
  }

  def updateCurrentConfig = Action { implicit request =>
    val loginForm = Form(
        "configFile" -> nonEmptyText
    )
    val (configFile) = loginForm.bindFromRequest()
    DashboardConfig.updateConfig(configFile.get)
    Redirect(routes.AdminEndpoints.viewCurrentConfig())
  }

  def viewConfigHistory = Action {
    Ok(
      views.html.admin.dashboardConfigHistory(DashboardConfig.getHistory())
    )
  }

  def deleteConfig(configName: String) = Action {
    DashboardConfig.deleteHistoryConfig(Option(configName))
    Redirect(routes.AdminEndpoints.viewConfigHistory())
  }

  def viewConfig(configName: String) = Action {
    Ok(Json.parse(
      DashboardConfig.viewConfig(Option(configName))
    ))
  }
}