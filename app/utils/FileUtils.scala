package utils

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

/**
 * Created by andrzej on 22/02/2015.
 */
object FileUtils {


  def createNewFile(filePath: String): Boolean = {
     val newFile: File = new File(filePath);
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
}
