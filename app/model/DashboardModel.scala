package model

import com.google.gson.Gson
import config.DashboardConfig

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
    def statusPageReturned403:Boolean = {
      statusPageResponse.httpStatus == 403
    }
  }

  case class WebPageResponse(content: String, httpStatus: Int, errorMessage: Option[String])

  def create(): Dashboard = {
    new Gson().fromJson(DashboardConfig.get().dashboardJson, classOf[Dashboard])
  }

  lazy val instance: Dashboard = create()

}
