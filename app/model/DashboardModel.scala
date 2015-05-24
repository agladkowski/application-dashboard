package model

import com.google.gson.Gson
import config.DashboardConfig
import org.jboss.netty.handler.codec.http.HttpResponseStatus

/**
 * Created by andrzej on 15/02/2015.
 *
 */
object DashboardModel {

  case class Dashboard(applications: Array[Application])

  case class Application(name: String, statusPageVersionRegex: String, hostRegex: String = "", environments: Array[Environment])

  case class Environment(name: String, statusPageUrl: String, optional: Boolean = false)

  case class ApplicationStatus(application: Application, environment: Environment, version: Option[String], host: Option[String], statusPageResponse: WebPageResponse) {
    val error: Boolean = statusPageResponse.errorMessage.isDefined
    val forbidden: Boolean = HttpResponseStatus.FORBIDDEN.getCode == statusPageResponse.httpStatus
    val timeout: Boolean = HttpResponseStatus.REQUEST_TIMEOUT.getCode == statusPageResponse.httpStatus
    val isVersionDefined: Boolean = !error && version.isDefined && !version.getOrElse("").startsWith("Invalid") && !version.getOrElse("").isEmpty
  }

  case class WebPageResponse(content: String, httpStatus: Int, errorMessage: Option[String])

  def create(): Dashboard = {
    new Gson().fromJson(DashboardConfig.get().dashboardJson, classOf[Dashboard])
  }

  lazy val instance: Dashboard = create()

}
