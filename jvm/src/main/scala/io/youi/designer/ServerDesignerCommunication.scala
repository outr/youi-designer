package io.youi.designer

import java.io.File

import io.circe.Printer
import io.youi.designer.model.Group
import org.matthicks.media4s.image.ImageUtil
import org.powerscala.io._
import profig.JsonUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ServerDesignerCommunication extends DesignerCommunication {
  override def saveImage(psdFileName: String, fileName: String, dataURL: String): Future[Unit] = Future {
    val outputPath = psdFileName.substring(0, psdFileName.indexOf('.'))
    val directory = new File(ServerDesignerApplication.outputDirectory, outputPath)
    directory.mkdirs()
    val file = new File(directory, fileName)
    ImageUtil.saveBase64(dataURL, file)
  }

  override def saveImport(psdFileName: String, layer: Group): Future[Unit] = Future {
    val outputPath = psdFileName.substring(0, psdFileName.indexOf('.'))
    val directory = new File(ServerDesignerApplication.outputDirectory, outputPath)
    directory.mkdirs()
    val file = new File(directory, "screen.json")
    val json = JsonUtil.toJson(layer)
    val jsonString = json.pretty(Printer.spaces2)
    IO.stream(jsonString, file)
  }
}