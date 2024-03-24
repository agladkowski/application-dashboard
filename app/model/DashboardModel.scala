package model

import com.google.gson._
import config.DashboardConfig
import play.api.http.Status

import java.net.URL


/**
 * Created by andrzej on 15/02/2015.
 *
 */
object DashboardModel extends Status{
  case class Dashboard(version: String = "1.1", applications: Array[Application])

  case class Application(name: String, attributes: Array[Attribute] = Array(), statusPageVersionRegex: String, hostRegex: String = "", environments: Array[Environment], links: Array[Link] = Array())

  case class Attribute(name: String, regex: String)

  case class Link(title: String, href: String) {
    def isRelative: Boolean = href.startsWith("/")
  }

  case class Environment(name: String, statusPageUrl: String, optional: Boolean = false) {
    def baseUrl = {
      val url = new URL(statusPageUrl)
      url.getProtocol + "://" + url.getAuthority
    }
  }

  case class ApplicationStatus(application: Application, environment: Environment, version: Option[String], host: Option[String], statusPageResponse: WebPageResponse,
                               attributes: Array[AttributeValue] = Array()) {
    val error: Boolean = statusPageResponse.errorMessage.isDefined
    val forbidden: Boolean = FORBIDDEN == statusPageResponse.httpStatus
    val timeout: Boolean = REQUEST_TIMEOUT == statusPageResponse.httpStatus
    val isVersionDefined: Boolean = !error && version.isDefined && !version.getOrElse("").startsWith("Invalid") && !version.getOrElse("").isEmpty
    val hasLinks = application.links != null
  }

  case class AttributeValue(name: String, value: String)

  case class WebPageResponse(content: String, httpStatus: Int, errorMessage: Option[String])

  def create(jsonContent: String = DashboardConfig.get().dashboardJson): Dashboard = {
    new GsonBuilder().create().fromJson(jsonContent, classOf[Dashboard])
  }

  lazy val instance: Dashboard = create()

}
