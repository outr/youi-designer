package io.youi.designer

import com.outr.psd.{PSD, PSDNode}
import io.youi._
import io.youi.app.screen.{PathActivation, UIScreen}
import io.youi.component.mixins.ScrollSupport
import io.youi.component.{Container, ImageView, TextView}
import io.youi.datatransfer.DataTransferManager
import io.youi.font.{Font, GoogleFont, OpenTypeFont}
import io.youi.image.{HTMLImage, Image}
import io.youi.paint.{Border, Paint, Stroke}
import org.scalajs.dom._
import profig.JsonUtil
import reactify._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ImportScreen extends UIScreen with PathActivation {
  private lazy val dataTransfer = new DataTransferManager

  private lazy val fontMapping = Map(
    "OpenSans" -> GoogleFont.`Open Sans`.regular,
    "OpenSans-Semibold" -> GoogleFont.`Open Sans`.`600`,
    "OpenSans-Bold" -> GoogleFont.`Open Sans`.`700`,
    "OpenSans-Extrabold" -> GoogleFont.`Open Sans`.`800`
  )

  def communication: DesignerCommunication = ClientDesignerApplication.communication(ClientDesignerApplication.clientConnectivity(ClientDesignerApplication.connectivity).connection)

  override def createUI(): Future[Unit] = OpenTypeFont.fromURL(GoogleFont.`Open Sans`.regular).map { font =>
    TextView.font.file := font
    TextView.font.size := 24.0
    TextView.fill := Color.Black

    val heading = new TextView {
      id := "heading"
      value := "Import Tool"
      font.size := 48.0
      fill := Color.SteelBlue
      position.center := container.position.center
      position.top := 10.0
    }

    val previewContainer = new Container with ScrollSupport {
      id := "previewContainer"
      position.top := heading.position.bottom + 10.0
      size.width := container.size.width - 5.0
      size.height := container.size.height - position.top - 5.0
      border := Border(Stroke(Color.Black))
    }
    val previewImage = new ImageView {
      id := "previewImage"
      position.center := container.position.center
    }
    val previewElements = new Container {
      id := "previewElements"
      position.top := 0.0
      position.center := previewContainer.size.center
    }
    previewContainer.children += previewImage
    previewContainer.children += previewElements

    container.children ++= Seq(heading, previewContainer)

    dataTransfer.addDragTarget(document.body)
    dataTransfer.overlay.visible.attach {
      case true => container.background := Paint.vertical(container.size.height).distributeColors(
        Color.AliceBlue,
        Color.LightBlue
      )
      case false => container.background := Paint.none
    }

    var psdFileName: String = ""
    var fileNames: Set[String] = Set.empty

    def generateFileName(name: String, increment: Int = 0): String = if (increment == 0) {
      generateFileName(name.replace(' ', '_').toLowerCase, increment + 1)
    } else {
      val fileName = if (increment > 1) {
        s"$name ($increment)"
      } else {
        name
      }
      if (!fileNames.contains(fileName)) {
        fileNames += fileName
        s"$fileName.png"
      } else {
        generateFileName(name, increment + 1)
      }
    }

    def process(node: PSDNode): Option[model.Entry] = {
      val export = node.export()
      lazy val textOption = export.text.toOption
      if (export.visible) {
        if (node.isGroup()) {
          val children = node.children().toList
          scribe.debug(s"Group: [${node.name}], Children: ${children.length}")
          val entries = processChildren(children)
          Some(model.Group(export.name, entries))
        } else if (textOption.nonEmpty) {
          val text = textOption.get
          val fontName = text.font.name.filterNot(c => c == 0 || c == 65279)
          val font = fontMapping.getOrElse(fontName, throw new RuntimeException(s"Unmapped font: [$fontName]"))
          val fontSize = text.font.sizes(0)
          val colors = text.font.colors(0)
          val fontColor = Color.fromRGBA(colors(0).toInt, colors(1).toInt, colors(2).toInt, (colors(3) / 255.0) * export.opacity)
          val t = new TextView
          t.value := text.value
          OpenTypeFont.fromURL(font).foreach(t.font.file := _)
          t.font.size := fontSize
          t.fill := fontColor
          val alignment = text.font.alignment.head match {
            case "center" => {
              t.position.center := export.left + (export.width / 2.0)
              Horizontal.Center
            }
            case "right" => {
              t.position.right := export.right
              Horizontal.Right
            }
            case _ => {
              t.position.left := export.left
              Horizontal.Left
            }
          }
          t.position.top := export.top
          previewElements.children += t
          Some(model.Text(export.name, text.value, fontName, fontSize, fontColor.value, alignment, export.left, export.top, export.width, export.height, export.opacity))
        } else {
          if (export.width > 0.0 && export.height > 0.0) {
            val view = new ImageView
            view.id := export.name
            previewElements.children += view

            val png = node.toPng()
            val img = model.Image(export.name, generateFileName(export.name), export.left, export.top, export.width, export.height, export.opacity)
            HTMLImage(png).foreach { image =>
              image.toDataURL.foreach { dataURL =>
                communication.saveImage(psdFileName, img.fileName, dataURL)
              }
              view.image := image
              view.position.left := export.left
              view.position.top := export.top
            }
            Some(img)
          } else {
            None
          }
        }
      } else {
        None
      }
    }

    def processChildren(nodes: List[PSDNode]): List[model.Entry] = nodes.reverse.flatMap(process)

    dataTransfer.fileReceived.attach { dtf =>
      val file = dtf.file
      PSD.fromFile(file).toFuture.foreach { psd =>
        try {
          psdFileName = file.name
          scribe.info(s"Processing $psdFileName...")
          val tree = psd.tree()
          HTMLImage(psd.image.toPng()).foreach { image =>
            previewElements.size.width := image.width
            previewElements.size.height := image.height
            image.toDataURL.foreach { dataURL =>
              communication.saveImage(psdFileName, "preview.png", dataURL)
            }
          }
          previewElements.children := Vector.empty
          fileNames = Set.empty
          val entries = processChildren(tree.children().toList)
          val root = model.Group("root", entries)
          scribe.info(JsonUtil.toJsonString(root))
          communication.saveImport(psdFileName, root)

          scribe.info(s"Finished processing $psdFileName!")
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