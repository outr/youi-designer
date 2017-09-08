package io.youi.designer

import io.youi.app.ServerApplication
import io.youi.http._

object ServerDesignerApplication extends ServerApplication with DesignerApplication {
  handler.matcher(
    combined.any(
      path.exact("/"),
      path.exact("/import")
    )
  ).page()

  override def main(args: Array[String]): Unit = start(args)
}
