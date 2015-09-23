package utils

import java.net.ConnectException

import model.DashboardModel.{Application, ApplicationStatus, Environment, WebPageResponse}
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.ws.WS

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}

/**
 * Created by andrzej on 01/03/2015.
 */
object RequestUtils {
  val defaultStatusPageRequestTimeout: Int = 2000 // IN MILLIS
  val defaultStatusPageCacheInSeconds: Int = 20 // IN SECONDS
  val logger: slf4j.Logger = LoggerFactory.getLogger(RequestUtils.getClass)

  def buildApplicationStatusPage(application: Application, environment: Environment): Future[ApplicationStatus] = {
    val webPageContentFuture: Future[WebPageResponse] = RequestUtils.getUrlContent(environment.statusPageUrl)
    webPageContentFuture.map { statusPageResponse: WebPageResponse =>
      val version = RegexUtils.findMatch(application.statusPageVersionRegex, statusPageResponse.content)
      val host = ProcessUtils.getHostDetails(environment.statusPageUrl, Option(application.hostRegex))
      ApplicationStatus(application, environment, Option(version), host, statusPageResponse)
    }
  }

  def getUrlContent(url: String): Future[WebPageResponse] = {
    val statusPageCacheInSeconds: Int = Play.configuration.getInt("status.cache.seconds").getOrElse(defaultStatusPageCacheInSeconds)
    val statusPageRequestTimeoutMillis: Int = Play.configuration.getInt("status.request.timeout.millis").getOrElse(defaultStatusPageRequestTimeout)
    logger.info(s"Loading ${url} timeout=${statusPageRequestTimeoutMillis}ms cache=${statusPageCacheInSeconds}sec")

    Cache.getOrElse[Future[WebPageResponse]](url, statusPageCacheInSeconds) {
      WS.url(url).withHeaders(buildRequestHeaders).withRequestTimeout(statusPageRequestTimeoutMillis).get().map { response =>
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
        case t: TimeoutException =>
          logger.error(s"TimeoutException ${url} - ${t.getMessage}")
          Future.successful(WebPageResponse("", HttpResponseStatus.REQUEST_TIMEOUT.getCode, Some(t.getMessage)))
        case t: ConnectException =>
          logger.error(s"ConnectException ${url} - ${t.getMessage}")
          Future.successful(WebPageResponse("", HttpResponseStatus.SERVICE_UNAVAILABLE.getCode, Some(t.getMessage)))
        case t: Throwable =>
          logger.error(s"Throwable ${url} - ${t.getMessage}", t)
          Future.successful(WebPageResponse("", -1, Some(t.getMessage)))
      }
    }
  }

  def buildRequestHeaders: (String, String) = {
    "User-Agent" -> "Dashboard/1.0 ()"
  }
}
