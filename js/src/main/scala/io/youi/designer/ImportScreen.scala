package io.youi.designer

import com.outr.psd.{PSD, PSDNode}
import io.youi._
import io.youi.component.mixins.ScrollSupport
import io.youi.component.{Container, ImageView, TextView}
import io.youi.datatransfer.DataTransferManager
import io.youi.image.HTMLImage
import io.youi.paint.{Border, Paint, Stroke}
import org.scalajs.dom._
import reactify._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ImportScreen extends DesignerScreen {
  private lazy val dataTransfer = new DataTransferManager

  override def createUI(): Future[Unit] = FontMap().map { fontMap =>
    TextView.font.file := fontMap("OpenSans")
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
          val fontSize = text.font.sizes(0)
          val colors = text.font.colors(0)
          val fontColor = Color.fromRGBA(colors(0).toInt, colors(1).toInt, colors(2).toInt, (colors(3) / 255.0) * export.opacity)
          val t = new TextView
          t.value := text.value
          t.font.file := fontMap(fontName)
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
          t.position.middle := export.top + (export.height / 2.0)

          previewElements.children += t
          Some(model.Text(export.name, text.value, fontName, fontSize, fontColor.value, alignment, t.position.left, t.position.top, export.opacity))
        } else {
          if (export.width > 0.0 && export.height > 0.0) {
            val view = new ImageView
            view.id := export.name
            previewElements.children += view

            val png = node.toPng()
            val fileName = FileName(s"${export.name}.png").deduplicate { fn =>
              val b = fileNames.contains(fn)
              if (!b) fileNames += fn
              b
            }.toString
            val img = model.Image(export.name, fileName, export.left, export.top, export.opacity)
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

            previewElements.children := Vector.empty
            fileNames = Set.empty
            val entries = processChildren(tree.children().toList)
            val root = model.Root(image.width, image.height, entries)
            communication.saveImport(psdFileName, root)

            scribe.info(s"Finished processing $psdFileName!")
          }
        } catch {
          case t: Throwable => t.printStackTrace()
        }
      }
    }
  }
}