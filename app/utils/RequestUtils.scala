package utils

import com.typesafe.config.ConfigFactory
import model.DashboardModel.{Application, ApplicationStatus, AttributeValue, Environment, WebPageResponse}
import org.slf4j
import org.slf4j.LoggerFactory
import play.api.cache.AsyncCacheApi
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.net.ConnectException
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS, SECONDS}
import scala.concurrent.{Future, TimeoutException}

/**
 * Created by andrzej on 01/03/2015.
 */
class RequestUtils @Inject() (cache: AsyncCacheApi, ws: WSClient) extends Status {
  val defaultStatusPageRequestTimeout: Int = 2000 // IN MILLIS
  val defaultStatusPageCacheInSeconds: Int = 20 // IN SECONDS
  val logger: slf4j.Logger = LoggerFactory.getLogger("RequestUtils")

  def buildApplicationStatusPage(application: Application, environment: Environment): Future[ApplicationStatus] = {
    val webPageContentFuture: Future[WebPageResponse] = getUrlContent(environment.statusPageUrl)
    webPageContentFuture.map { statusPageResponse: WebPageResponse =>
      val version = RegexUtils.findMatch(application.statusPageVersionRegex, statusPageResponse.content)
      val host = ProcessUtils.getHostDetails(environment.statusPageUrl, Option(application.hostRegex))
      if (application.attributes != null)
        {
          val attributeValues = application.attributes.map(a => {
            val attributeValue = RegexUtils.findMatch(a.regex, statusPageResponse.content)
            AttributeValue(a.name, attributeValue)
          })
          ApplicationStatus(application, environment, Option(version), host, statusPageResponse, attributeValues)
        }
      else
        {
          ApplicationStatus(application, environment, Option(version), host, statusPageResponse)
        }
    }
  }

  def getConfiguration(name: String, defaultValue: String): String = {
    System.getProperty(name, defaultValue)
  }

  def getUrlContent(url: String): Future[WebPageResponse] = {
    val statusPageCacheInSeconds: Int = ConfigFactory.load().getInt("status.cache.seconds") //.getOrElse(defaultStatusPageCacheInSeconds)
    val statusPageRequestTimeoutMillis: Int = ConfigFactory.load().getInt("status.request.timeout.millis") //.getOrElse(defaultStatusPageRequestTimeout)
    logger.info(s"Loading ${url} timeout=${statusPageRequestTimeoutMillis}ms cache=${statusPageCacheInSeconds}sec")

    cache.getOrElseUpdate[WebPageResponse](url, Duration(statusPageCacheInSeconds, SECONDS)) {
      ws.url(url).withHeaders(buildRequestHeaders)
        .withRequestTimeout(Duration(statusPageRequestTimeoutMillis, MILLISECONDS)).get().map { response =>
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
          Future.successful(WebPageResponse("", REQUEST_TIMEOUT, Some(t.getMessage)))
        case t: ConnectException =>
          logger.error(s"ConnectException ${url} - ${t.getMessage}")
          Future.successful(WebPageResponse("", SERVICE_UNAVAILABLE, Some(t.getMessage)))
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
