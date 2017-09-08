package io.youi.designer

import io.youi.app.ClientApplication

import scala.scalajs.js.annotation.JSExportTopLevel

object ClientDesignerApplication extends ClientApplication with DesignerApplication {
  val importScreen: ImportScreen.type = ImportScreen

  @JSExportTopLevel("application")
  def main(): Unit = scribe.info("Initialized!")
}