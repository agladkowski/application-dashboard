package utils

import java.net.URL

import org.slf4j.LoggerFactory

import scala.sys.process._

/**
 * Created by andrzej on 19/02/2015.
 */
object ProcessUtils {
  val OS: String = System.getProperty("os.name")
  val logger: org.slf4j.Logger = LoggerFactory.getLogger(ProcessUtils.getClass)

  def getHostDetails(url: String, hostRegex: Option[String]): Option[String] = {
    val host: String = new URL(url).getHost

    if (hostRegex.isEmpty) return Option(host)

    try {
      val commandResultStr: Option[String] = Option(s"${getCommand()} ${host}".!!)

      if (commandResultStr.isEmpty) return Option(host)

      val hostMatchingRegex: String = RegexUtils.findMatch(hostRegex.get.r, commandResultStr.get)

      if (hostMatchingRegex.isEmpty) return Option(host)

      return Option(hostMatchingRegex)
    } catch {
      case e: Exception => logger.info(s"Unable to execute ${getCommand()}")
    }
    return Option(host)
  }

  def getCommand(): String = {
    if (OS.startsWith("Windows")) "nslookup" else "host"
  }
}