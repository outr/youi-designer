package io.youi.designer

import io.youi._
import io.youi.component.{Container, ImageView, TextView}
import io.youi.designer.model._
import io.youi.image.HTMLImage

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PreviewScreen extends DesignerScreen with VirtualSizeSupport {
  override def createUI(): Future[Unit] = FontMap().map { fontMap =>
    val font = fontMap("OpenSans")
    TextView.font.family := font
    TextView.font.weight := font
    TextView.font.size := 24.0
    TextView.color := Color.Black
    virtualMode := VirtualMode.Bars

    History.url().param("screen") match {
      case Some(screenName) => {
        load(fontMap, screenName)
      }
      case None => container.children += new TextView {
        value := "No screen specified! Path should be: /preview?screen=screenName"
        position.center := container.position.center
        position.middle := container.position.middle
      }
    }
  }

  def load(fontMap: FontMap, screenName: String): Unit = communication.loadScreen(screenName).foreach {
    case Some(group) => render(fontMap, group, container)
    case None => scribe.warn(s"Screen not found: $screenName")
  }

  private def render(fontMap: FontMap, entry: Entry, container: Container): Unit = entry match {
    case Image(id, fileName, x, y, opacity) => {
      val v = new ImageView
      HTMLImage(History.url().withPart(s"/working/assets/$fileName")).foreach { image =>
        image.resize(image.width.vw, image.height.vh).foreach { resized =>
          v.image := resized
        }
      }
      v.id := id
      v.position.x := x.vx
      v.position.y := y.vy
      v.opacity := opacity
      v.event.click.on(scribe.info(s"Image clicked: $id."))
      container.children += v
    }
    case Text(id, text, font, size, color, alignment, x, y, opacity) => {
      val v = new TextView
      v.id := id
      v.value := text
      val fontWeight = fontMap(font)
      v.font.family := fontWeight
      v.font.weight := fontWeight
      v.font.size := size.vf
      v.color := Color.fromLong(color)
      v.position.x := x.vx
      v.position.y := y.vy
      v.opacity := opacity
      v.event.click.on(scribe.info(s"Text clicked: $id."))

      alignment match {
        case Horizontal.Left => // Nothing to do
        case Horizontal.Center => v.position.center.static(v.position.center)
        case Horizontal.Right => v.position.right.static(v.position.right)
      }

      container.children += v
    }
    case Group(id, children) => {
      val c = new Container
      c.id := id
      container.children += c
      children.foreach(render(fontMap, _, c))
    }
    case Root(width, height, children) => {
      virtualWidth := width
      virtualHeight := height
      children.foreach(render(fontMap, _, container))
    }
  }
}
