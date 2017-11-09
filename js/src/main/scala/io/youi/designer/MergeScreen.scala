package io.youi.designer

import io.youi.{Color, ui}
import io.youi.app.screen.{PathActivation, UIScreen}
import io.youi.component.mixins.ScrollSupport
import io.youi.component._
import io.youi.font.{GoogleFont, OpenTypeFont}
import io.youi.image.Image
import io.youi.layout.{FlowLayout, Margins, VerticalLayout}
import io.youi.paint.{Border, Paint, Stroke}
import io.youi.util.SizeUtility
import reactify._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MergeScreen extends DesignerScreen {
  private lazy val mergeButton = new Button("Merge Selected") {
    position.left := 25.0
    position.top := 10.0
  }

  private lazy val previews = new TypedContainer[ConversionPreview] with ScrollSupport {
    position.top := 100.0
    size.width := ui.width
    size.height := ui.height
    layout := new FlowLayout(Margins(5.0, 5.0, 5.0, 5.0))
  }

  override def createUI(): Future[Unit] = for {
    directories <- communication.listConversions()
    font <- OpenTypeFont.fromURL(GoogleFont.`Open Sans`.regular)
  } yield {
    TextView.font.file := font
    TextView.font.size := 24.0
    TextView.fill := Color.Black
    directories.foreach { directory =>
      val preview = new ConversionPreview(directory)
      previews.children += preview
    }

    mergeButton.event.click.on(merge())

    container.children += mergeButton
    container.children += previews
  }

  def merge(): Unit = {
    val directories = previews.children().collect {
      case preview if preview.selected() => preview.directory
    }.toList
    communication.mergeConversions(directories)
  }
}

class ConversionPreview(val directory: String) extends Container {
  val selected: Var[Boolean] = Var(true)

  private val heading = new TextView {
    value := directory
    fill := Color.SteelBlue
  }

  private val preview = new ImageView {
    Image(s"/$directory/preview.png").map { img =>
      val scaled = SizeUtility.scale(img.width, img.height, 200.0, 900.0)
      img.resize(scaled.width, scaled.height).foreach { resized =>
        image := resized
      }
    }
  }

  layout := new VerticalLayout(10.0)
  border := Border(Stroke(Color.Black))
  padding := 5.0

  background := {
    if (event.pointer.overState()) {
      Paint.vertical(size.height).distributeColors(
        Color.AliceBlue,
        Color.LightBlue
      )
    } else if (selected()) {
      Color.LightBlue
    } else {
      Paint.none
    }
  }

  event.click.on {
    selected.static(!selected)
  }

  children += heading
  children += preview
}

class Button extends Container {
  def this(value: String) = {
    this()
    text.value := value
  }

  val text: TextView = new TextView

  padding := 15.0
  border := Border(Stroke(Color.Black), 5.0)
  background := {
    if (event.pointer.downState()) {
      Paint.vertical(size.height).distributeColors(
        Color.LightBlue,
        Color.AliceBlue
      )
    } else if (event.pointer.overState()) {
      Paint.vertical(size.height).distributeColors(
        Color.AliceBlue,
        Color.LightBlue
      )
    } else {
      Paint.vertical(size.height).distributeColors(
        Color.White,
        Color.LightGray
      )
    }
  }
  children += text
}