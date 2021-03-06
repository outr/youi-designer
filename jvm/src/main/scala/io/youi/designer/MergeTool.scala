package io.youi.designer

import java.io.File

import io.youi.designer.model._
import org.powerscala.io._
import profig.JsonUtil

import scala.annotation.tailrec
import com.roundeights.hasher.Implicits._
import io.circe.Printer

import scala.language.postfixOps

class MergeTool(directories: List[File]) {
  private var hashMap = Map.empty[String, File]

  def merge(): Unit = {
    IO.delete(ServerDesignerApplication.working)
    ServerDesignerApplication.assets.mkdirs()

    mergeRecursive(directories)
  }

  @tailrec
  private def mergeRecursive(directories: List[File]): Unit = if (directories.nonEmpty) {
    mergeDirectory(directories.head)

    mergeRecursive(directories.tail)
  }

  private def mergeDirectory(directory: File): Unit = {
    val screen = new File(directory, "screen.json")
    val entry = JsonUtil.fromJsonString[Root](IO.stream(screen, new StringBuilder).toString)
    processEntry(directory, entry)

    val json = JsonUtil.toJson(entry)
    val jsonString = json.pretty(Printer.spaces2)
    val jsonFileName = FileName(s"${directory.getName}.json")
    IO.stream(jsonString, new File(ServerDesignerApplication.working, jsonFileName.toString))
  }

  private def processEntry(directory: File, entry: Entry): Unit = entry match {
    case i: Image => {
      val file = new File(directory, i.fileName)
      val sha1 = file.sha1.hex
      hashMap.get(sha1) match {
        case Some(existing) => i.fileName = existing.getName
        case None => {
          val fileName = FileName(i.fileName).deduplicate { fn =>
            new File(ServerDesignerApplication.assets, fn).exists()
          }
          i.fileName = fileName.toString
          val asset = new File(ServerDesignerApplication.assets, fileName.toString)
          if (asset.exists()) scribe.warn(s"Asset already exists! ${i.fileName}")
          IO.stream(file, asset)
          hashMap += sha1 -> asset
        }
      }
    }
    case _: Text => // Ignore
    case g: Group => g.children.foreach(processEntry(directory, _))
    case r: Root => r.children.foreach(processEntry(directory, _))
  }
}