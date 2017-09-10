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
import scala.scalajs.js

object ImportScreen extends UIScreen with PathActivation {
  private lazy val dataTransfer = new DataTransferManager

  private lazy val fontMapping = Map(
    "OpenSans" -> GoogleFont.`Open Sans`.regular,
    "OpenSans-Bold" -> GoogleFont.`Open Sans`.`700`
  )

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
      lazy val textOption = export.text.toOption
      if (export.visible) {
        if (node.isGroup()) {
          val children = node.children().toList
          scribe.debug(s"Group: [${node.name}], Children: ${children.length}")
          processChildren(children)
        } else if (textOption.nonEmpty) {
          val text = textOption.get
          val fontName = text.font.name.filterNot(c => c == 0 || c == 65279)
          val font = fontMapping.getOrElse(fontName, throw new RuntimeException(s"Unmapped font: [$fontName]"))
          val fontSize = text.font.sizes(0)
          val colors = text.font.colors(0)
          val fontColor = Color.fromRGBA(colors(0).toInt, colors(1).toInt, colors(2).toInt, (colors(3) / 255.0) * export.opacity)
          val t = new Text
          t.value := text.value
          t.font.file := Font.fromURL(font)
          t.font.size := fontSize
          t.fill := fontColor
          text.font.alignment.head match {
            case "center" => {
              t.position.center := export.left + (export.width / 2.0)
            }
            case "right" => {
              t.position.right := export.right
            }
            case _ => {
              t.position.left := export.left
            }
          }
          t.position.top := export.top
          previewElements.children += t
          scribe.info(s"Text: [${node.name}], Text: ${text.value}, Opacity: ${export.opacity}, Export: ${JSON.stringify(export)}")
          scribe.info(s"Test: ${JSON.stringify(node.asInstanceOf[js.Dynamic].get("typeTool").engineData)}")
        } else {
          scribe.debug(s"Layer: [${node.name}], Left: ${export.left}, Top: ${export.top}")
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