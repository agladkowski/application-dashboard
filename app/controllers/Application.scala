package controllers

import model.DashboardModel.{Environment, _}
import model._
import play.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.ws.WS
import play.api.mvc._
import utils.{ProcessUtils, RegexUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {

  def application(applicationName: String) = Action.async {
    buildDashboardModel(Option(applicationName)).map { applicationStatus =>
      Ok(views.html.application(applicationStatus))
    }
  }

  def environment(environmentName: String) = Action.async {
    buildDashboardModel(Option.empty, Option(environmentName)).map { applicationStatus =>
      Ok(views.html.environment(applicationStatus))
    }
  }

  def applications = Action.async {
    buildDashboardModel().map { statusList =>
      Ok(views.html.applications(statusList))
    }
  }

  def environments = Action.async {
    buildDashboardModel().map { statusList =>
      Ok(views.html.environments(statusList))
    }
  }

  // Utility methods

  def buildDashboardModel(applicationName: Option[String] = Option.empty, environmentName: Option[String] = Option.empty): Future[List[ApplicationStatus]] = {
    val applicationFilter: (Application) => Boolean = { application => if (applicationName.isDefined) application.name == applicationName.get else true }
    val applicationList: List[Application] = DashboardModel.create().applications.filter(applicationFilter).toList
    val applicationStatusFutureList: List[Future[ApplicationStatus]] = applicationList.flatMap { application =>
      for {
        environment <- application.environments
        if environmentName.isEmpty || environment.name == environmentName.get
      } yield {
        buildApplicationStatusPage(application, environment)
      }
    }

    val statusFutureList: Future[List[ApplicationStatus]] = Future.sequence(applicationStatusFutureList).map(_.toList)
    statusFutureList
  }

  def buildApplicationStatusPage(application: Application, environment: Environment): Future[ApplicationStatus] = {
    val webPageContentFuture: Future[WebPageResponse] = getUrlContent(environment.statusPageUrl)
    webPageContentFuture.map { statusPageResponse: WebPageResponse =>
      val version = RegexUtils.findMatch(application.statusPageVersionRegex.r, statusPageResponse.content)
      val host = ProcessUtils.getHostDetails(environment.statusPageUrl, Option(application.hostRegex))
      ApplicationStatus(application, environment, Option(version), host, statusPageResponse)
    }
  }

  val STATUS_PAGE_REQUEST_TIMEOUT: Int = 2000 // IN SECONDS
  val STATUS_PAGE_CACHE_IN_SECONDS: Int = 20 // IN SECONDS

  def getUrlContent(url: String): Future[WebPageResponse] = {
    Logger.info(s"Loading ${url}")
    Cache.getOrElse[Future[WebPageResponse]](url, STATUS_PAGE_CACHE_IN_SECONDS) {
      WS.url(url).withHeaders(buildRequestHeaders).withRequestTimeout(STATUS_PAGE_REQUEST_TIMEOUT).get().map { response =>
        if (200 == response.status) {
          WebPageResponse(response.body, response.status, Option.empty)
        } else if (404 == response.status) {
          WebPageResponse(response.body, response.status, Option("Status page returned 404 - Not Found"))
        } else if (403 == response.status) {
          WebPageResponse(response.body, response.status, Option("Status page returned 403 - Forbidden"))
        } else {
          WebPageResponse(response.body, response.status, Option(response.statusText))
        }
      }.recoverWith {
        case t: Throwable =>
          Logger.error(s"${url} - ${t.getMessage}", t)
          Future.successful(WebPageResponse("", -1, Option(t.getMessage)))
      }
    }
  }

  def buildRequestHeaders: (String, String) = {
    "User-Agent" -> "Dashboard/1.0"
  }
}