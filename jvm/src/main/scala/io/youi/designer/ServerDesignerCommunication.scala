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

  override def listConversions(): Future[List[String]] = Future.successful {
    ServerDesignerApplication.outputDirectory.listFiles().collect {
      case f if f.isDirectory && f.getName != "working" => f.getName
    }.toList.sorted
  }

  override def mergeConversions(directories: List[String]): Future[Unit] = Future {
    scribe.info(s"Merging: $directories")
    val tool = new MergeTool(directories.map(new File(ServerDesignerApplication.outputDirectory, _)))
    tool.merge()
  }

  override def loadScreen(screenName: String): Future[Option[Group]] = Future {
    val jsonFile = new File(ServerDesignerApplication.working, s"$screenName.json")
    if (jsonFile.isFile) {
      val jsonString = IO.stream(jsonFile, new StringBuilder).toString
      Option(JsonUtil.fromJsonString[Group](jsonString))
    } else {
      None
    }
  }
}