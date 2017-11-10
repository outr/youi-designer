package io.youi.designer

import io.youi.app.ClientApplication
import io.youi.util.DebugSupport

import scala.scalajs.js.annotation.JSExportTopLevel

object ClientDesignerApplication extends ClientApplication with DesignerApplication {
  lazy val debug: DebugSupport = new DebugSupport

  val importScreen: ImportScreen.type = ImportScreen
  val mergeScreen: MergeScreen.type = MergeScreen
  val previewScreen: PreviewScreen.type = PreviewScreen

  @JSExportTopLevel("application")
  def main(): Unit = {
    scribe.info("Initialized!")
    debug.enabled := true
  }
}