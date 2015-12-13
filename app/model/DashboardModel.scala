package model

import java.net.URL

import com.google.gson._
import config.DashboardConfig
import org.jboss.netty.handler.codec.http.HttpResponseStatus


/**
 * Created by andrzej on 15/02/2015.
 *
 */
object DashboardModel {
  case class Dashboard(version: String = "1.1", applications: Array[Application])

  case class Application(name: String, statusPageVersionRegex: String, hostRegex: String = "", environments: Array[Environment], links: Array[Link] = Array())

  case class Link(title: String, href: String) {
    def isRelative: Boolean = href.startsWith("/")
  }

  case class Environment(name: String, statusPageUrl: String, optional: Boolean = false) {
    def baseUrl = {
      val url = new URL(statusPageUrl)
      url.getProtocol + "://" + url.getAuthority
    }
  }

  case class ApplicationStatus(application: Application, environment: Environment, version: Option[String], host: Option[String], statusPageResponse: WebPageResponse) {
    val error: Boolean = statusPageResponse.errorMessage.isDefined
    val forbidden: Boolean = HttpResponseStatus.FORBIDDEN.getCode == statusPageResponse.httpStatus
    val timeout: Boolean = HttpResponseStatus.REQUEST_TIMEOUT.getCode == statusPageResponse.httpStatus
    val isVersionDefined: Boolean = !error && version.isDefined && !version.getOrElse("").startsWith("Invalid") && !version.getOrElse("").isEmpty
    val hasLinks = application.links != null
  }

  case class WebPageResponse(content: String, httpStatus: Int, errorMessage: Option[String])

  def create(jsonContent: String = DashboardConfig.get().dashboardJson): Dashboard = {
    new GsonBuilder().create().fromJson(jsonContent, classOf[Dashboard])
  }

  lazy val instance: Dashboard = create()

}
