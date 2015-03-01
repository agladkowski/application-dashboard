package utils

import model.DashboardModel.{Application, ApplicationStatus, Environment, WebPageResponse}
import play.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by andrzej on 01/03/2015.
 */
object RequestUtils {
  val STATUS_PAGE_REQUEST_TIMEOUT: Int = 2000 // IN MILLIS
  val STATUS_PAGE_CACHE_IN_SECONDS: Int = 20 // IN SECONDS

  def buildApplicationStatusPage(application: Application, environment: Environment): Future[ApplicationStatus] = {
    val webPageContentFuture: Future[WebPageResponse] = RequestUtils.getUrlContent(environment.statusPageUrl)
    webPageContentFuture.map { statusPageResponse: WebPageResponse =>
      val version = RegexUtils.findMatch(application.statusPageVersionRegex, statusPageResponse.content)
      val host = ProcessUtils.getHostDetails(environment.statusPageUrl, Option(application.hostRegex))
      ApplicationStatus(application, environment, Option(version), host, statusPageResponse)
    }
  }

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
