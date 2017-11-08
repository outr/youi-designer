package io.youi.designer

import io.youi.{Color, ui}
import io.youi.app.screen.{PathActivation, UIScreen}
import io.youi.component.mixins.ScrollSupport
import io.youi.component.{Container, ImageView, TextView, TypedContainer}
import io.youi.font.{GoogleFont, OpenTypeFont}
import io.youi.image.Image
import io.youi.layout.{FlowLayout, Margins, VerticalLayout}
import io.youi.paint.{Border, Paint, Stroke}
import io.youi.util.SizeUtility
import reactify._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MergeScreen extends UIScreen with PathActivation {
  def communication: DesignerCommunication = ClientDesignerApplication.communication(ClientDesignerApplication.clientConnectivity(ClientDesignerApplication.connectivity).connection)

  private lazy val previews = new TypedContainer[ConversionPreview] with ScrollSupport {
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
    container.children += previews
  }

  override def path: String = "/merge"
}

class ConversionPreview(directory: String) extends Container {
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
  padding.left := 5.0
  padding.right := 5.0
  padding.top := 5.0
  padding.bottom := 5.0

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