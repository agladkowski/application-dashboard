package model

import com.google.gson.Gson
import config.DashboardConfig
import org.jboss.netty.handler.codec.http.HttpResponseStatus

/**
 * Created by andrzej on 15/02/2015.
 */
object DashboardModel {

  case class Dashboard(applications: Array[Application])

  case class Application(name: String, statusPageVersionRegex: String, hostRegex: String = "", environments: Array[Environment])

  case class Environment(name: String, statusPageUrl: String)

  case class ApplicationStatus(application: Application, environment: Environment, version: Option[String], host: Option[String], statusPageResponse: WebPageResponse) {
    def isError:Boolean = {
      statusPageResponse.errorMessage.isDefined
    }
    def forbiddenAccess:Boolean = {
      statusPageResponse.httpStatus == HttpResponseStatus.FORBIDDEN.getCode
    }
    def timeout:Boolean = {
      statusPageResponse.httpStatus == HttpResponseStatus.REQUEST_TIMEOUT.getCode
    }
  }

  case class WebPageResponse(content: String, httpStatus: Int, errorMessage: Option[String])

  def create(): Dashboard = {
    new Gson().fromJson(DashboardConfig.get().dashboardJson, classOf[Dashboard])
  }

  lazy val instance: Dashboard = create()

}
