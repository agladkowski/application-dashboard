package controllers

import play.Logger
import play.api.libs.json._
import play.api.mvc._


object TestStatusPagesController extends Controller {
  def status(appName: String = "AppName", versionParamName: String = "version", version: String = "1.0.0") = Action {
    generateStatusPage(appName, versionParamName, version)
  }

  def timeout = Action {
    Logger.info("/timeout")
    Thread.sleep(2000)
    generateStatusPage("AppName","version", "2.0.0-SNAPSHOT")
  }

  def generateStatusPage(applicationName: String, versionParamName: String, version: String): Result = {
    Ok(Json.toJson(
      Map("status" -> "OK", "appName" -> applicationName, versionParamName -> version)
    ))
  }
}
