package io.youi.designer

import java.io.File

import io.youi.app.ServerApplication
import io.youi.http._
import profig.Config

object ServerDesignerApplication extends ServerApplication with DesignerApplication {
  lazy val outputDirectory: File = new File(Config("designer.output").as[Option[String]].getOrElse("output"))
  lazy val working = new File(ServerDesignerApplication.outputDirectory, "working")
  lazy val assets = new File(working, "assets")

  handler.matcher(
    combined.any(
      path.exact("/"),
      path.exact("/import"),
      path.exact("/merge"),
      path.exact("/preview")
    )
  ).page()

  handler.file(outputDirectory)

  override def main(args: Array[String]): Unit = start(args)
}