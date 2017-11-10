package io.youi.designer

import io.youi.{Color, History, Horizontal}
import io.youi.component.{Container, ImageView, TextView}
import io.youi.designer.model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PreviewScreen extends DesignerScreen {
  override def createUI(): Future[Unit] = FontMap().map { fontMap =>
    TextView.font.file := fontMap("OpenSans")
    TextView.font.size := 24.0
    TextView.fill := Color.Black

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
      val v = new ImageView(s"/working/assets/$fileName")
      v.id := id
      v.position.x := x
      v.position.y := y
      v.opacity := opacity
      container.children += v
    }
    case Text(id, text, font, size, color, alignment, x, y, opacity) => {
      val v = new TextView
      v.id := id
      v.value := text
      v.font.file := fontMap(font)
      v.font.size := size
      v.fill := Color.fromLong(color)
      v.position.x := x
      v.position.y := y
      v.opacity := opacity

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
  }
}
