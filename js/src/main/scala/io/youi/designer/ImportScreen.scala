package io.youi.designer

import com.outr.psd.PSD
import io.youi._
import io.youi.app.screen.{PathActivation, UIScreen}
import io.youi.component.{Container, ImageView, ScrollSupport, Text}
import io.youi.datatransfer.DataTransferManager
import io.youi.font.{Font, GoogleFont}
import io.youi.image.Image
import io.youi.paint.Paint
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
      value := "Import Tool"
      font.size := 48.0
      fill := Color.SteelBlue
      position.center := container.position.center
      position.top := 10.0
    }

    val previewContainer = new Container with ScrollSupport {
      position.top := heading.position.bottom + 10.0
      size.width := container.size.width
      size.height := container.size.height - position.top

      size.height.and(size.measured.height).on {
        scribe.info(s"Container height: ${size.height()}, Measured: ${size.measured.height()}")
      }
    }
    val preview = new ImageView {
      position.center := container.position.center
    }
    previewContainer.children += preview

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

    dataTransfer.fileReceived.attach { dtf =>
      val file = dtf.file
      PSD.fromFile(file).toFuture.foreach { psd =>
        try {
          scribe.info(s"Processing ${file.name}...")
          val tree = psd.tree()
          Image.fromImage(psd.image.toPng(), None, None).foreach { image =>
            preview.image := image
          }
//          val preview = psd.image.toPng()
          // TODO: show the image

//          val data = document.getElementById("data")
//          data.innerHTML = JSON.stringify(tree.export(), space = 2)

          tree.descendants().toList.foreach { node =>
            if (node.isGroup()) {
              scribe.info(s"${node.name} (group)")
            } else {
              scribe.info(node.name)
              val export = node.export()
//              val png = node.toPng()
              export.text.toOption match {
                case Some(text) => {
                  scribe.info(s"Text: ${text.value} (${text.font.name}, ${text.font.sizes}, ${text.font.colors}, ${text.font.alignment})")
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
          }

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