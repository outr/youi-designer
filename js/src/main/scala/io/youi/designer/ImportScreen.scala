package io.youi.designer

import com.outr.psd.{PSD, PSDNode}
import io.youi._
import io.youi.app.screen.{PathActivation, UIScreen}
import io.youi.component.{Container, ImageView, ScrollSupport, Text}
import io.youi.datatransfer.DataTransferManager
import io.youi.font.{Font, GoogleFont}
import io.youi.image.Image
import io.youi.paint.{Border, Paint, Stroke}
import org.scalajs.dom._
import reactify._

import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global

object ImportScreen extends UIScreen with PathActivation {
  private lazy val dataTransfer = new DataTransferManager

  override def createUI(): Unit = {
    Text.font.file := Font.fromURL(GoogleFont.`Open Sans`.regular)
    Text.font.size := 24.0
    Text.fill := Color.Black

    val heading = new Text {
      id := Some("heading")
      value := "Import Tool"
      font.size := 48.0
      fill := Color.SteelBlue
      position.center := container.position.center
      position.top := 10.0
    }

    val previewContainer = new Container with ScrollSupport {
      id := Some("previewContainer")
      position.top := heading.position.bottom + 10.0
      size.width := container.size.width - 5.0
      size.height := container.size.height - position.top - 5.0
      border := Border(Stroke(Color.Black))
    }
    val previewImage = new ImageView {
      id := Some("previewImage")
      position.center := container.position.center
    }
    val previewElements = new Container {
      id := Some("previewElements")
      position.top := 0.0
      position.center := previewContainer.size.center
      background := Color.LightSalmon
    }
    previewContainer.children += previewImage
    previewContainer.children += previewElements

    container.children ++= Seq(heading, previewContainer)

    dataTransfer.addDragTarget(document.body)
    dataTransfer.overlay.visible.attach {
      case true => container.background := Paint.vertical(container).distributeColors(
        Color.AliceBlue,
        Color.LightBlue
      )
      case false => container.background := Paint.none
    }

    val canvas = document.createElement("canvas").asInstanceOf[html.Canvas]

    def process(node: PSDNode): Unit = {
      val export = node.export()
      lazy val text = export.text.toOption
      if (export.visible) {
        if (node.isGroup()) {
          val children = node.children().toList
          scribe.info(s"Group: [${node.name}], Children: ${children.length}")
          processChildren(children)
//        } else if (text.nonEmpty) {
//          scribe.info(s"Text: [${node.name}], Text: ${text.get.value}, Font: ${text.get.font.name}")
        } else {
          scribe.info(s"Layer: [${node.name}], Left: ${export.left}, Top: ${export.top}")
          if (export.width > 0.0 && export.height > 0.0) {
            val view = new ImageView
            view.id := Some(export.name)
            previewElements.children += view
            Image.fromImage(node.toPng(), None, None).foreach { image =>
              view.image := image
              view.position.left := export.left
              view.position.top := export.top
            }
          }
        }
      } else {
        scribe.info(s"Ignoring invisible: ${node.name}")
      }
    }

    def processChildren(nodes: List[PSDNode]): Unit = nodes.reverse.foreach(process)

    dataTransfer.fileReceived.attach { dtf =>
      val file = dtf.file
      PSD.fromFile(file).toFuture.foreach { psd =>
        try {
          scribe.info(s"Processing ${file.name}...")
          val tree = psd.tree()
          Image.fromImage(psd.image.toPng(), None, None).foreach { image =>
//            previewImage.image := image
            previewElements.size.width := image.width
            previewElements.size.height := image.height
          }

//          val data = document.getElementById("data")
//          scribe.info(JSON.stringify(tree.export(), space = 2))

          previewElements.children := Vector.empty
          processChildren(tree.children().toList)
          /*tree.descendants().toList.foreach { node =>
            if (node.isGroup()) {
//              scribe.info(s"${node.name} (group)")
            } else {
//              scribe.info(node.name)
              val export = node.export()
//              val png = node.toPng()
              export.text.toOption match {
                case Some(text) => {
//                  scribe.info(s"Text: ${text.value} (${text.font.name}, ${text.font.sizes}, ${text.font.colors}, ${text.font.alignment})")
                }
                case None => {
//                  png.addEventListener("load", (evt: Event) => {
//                    canvas.width = png.width
//                    canvas.height = png.height
//                    val ctx = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
//                    ctx.drawImage(png, 0, 0, png.width, png.height)
//                    val imageData = ctx.getImageData(0, 0, png.width, png.height)
//
//                    val colors = imageData.data.toIterable.grouped(4).map { i =>
//                      val v = i.toVector
//                      Pixel(v(0), v(1), v(2), v(3))
//                    }.toSet
//
//                    val text = if (colors.size < 10) {
//                      s"${colors.mkString(", ")} (${colors.size})"
//                    } else {
//                      colors.size.toString
//                    }
//                    scribe.info(s"Colors: $text")
//                  })
                }
              }
//              div.appendChild(png)
//              images.appendChild(div)
            }
          }*/

          scribe.info(s"Finished processing ${file.name}!")
        } catch {
          case t: Throwable => t.printStackTrace()
        }
      }
    }
  }

  override def path: String = "/import"
}

case class Pixel(red: Int, green: Int, blue: Int, alpha: Int) {
  override def toString: String = s"Pixel(red: $red, green: $green, blue: $blue, alpha: $alpha)"
}