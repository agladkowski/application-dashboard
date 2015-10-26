package utils

import java.io.{PrintWriter, File}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.slf4j
import org.slf4j.LoggerFactory

import scala.io.BufferedSource

/**
 * Created by andrzej on 22/02/2015.
 */
object FileUtils {
  val logger: slf4j.Logger = LoggerFactory.getLogger("FileUtils")

  def createNewFile(targetFile: File, content: String): File = {
    createNewFile(targetFile.getAbsolutePath)
    val pw = new PrintWriter(targetFile)
    pw.write(content)
    pw.close

    targetFile
  }

  def createNewFile(targetFile: String): Boolean = {
    logger.debug("Creating new file " + targetFile)
    val newFile: File = new File(targetFile);
    newFile.getParentFile.mkdirs()
    newFile.createNewFile()
   }

  def save(filePath: String, fileContent: String) = {
    createNewFile(filePath)
    Files.write(Paths.get(filePath), fileContent.getBytes(StandardCharsets.UTF_8))
  }

  def delete(fileToDelete: String) = {
    val file = new File(fileToDelete)
    if (file.exists()) file.delete()
  }
  
  def listFiles(directory: String): Array[File] = {
    val dir = new File(directory)
    if (dir.exists()) dir.listFiles().filter(f => f.isFile)
    else Array.empty
  }

  def readFile(filePath: File): String = {
    val fileSource: BufferedSource = scala.io.Source.fromFile(filePath)
    val fileContent: String = fileSource.mkString
    fileSource.close
    return fileContent
  }
}
