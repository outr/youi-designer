package io.youi.designer

import io.youi._
import io.youi.app.ClientApplication
import io.youi.util.DebugSupport

import scala.scalajs.js.annotation.JSExportTopLevel

object ClientDesignerApplication extends ClientApplication with DesignerApplication {
  lazy val debug: DebugSupport = new DebugSupport(ui.renderer)
  val importScreen: ImportScreen.type = ImportScreen

  @JSExportTopLevel("application")
  def main(): Unit = {
    scribe.info("Initialized!")
    debug.enabled := true
  }
}