package io.youi.designer

import io.youi.font.{Font, GoogleFont, OpenTypeFont}

import scala.concurrent.Future

object FontMap {
  private lazy val map = Map(
    "OpenSans" -> GoogleFont.`Open Sans`.regular,
    "OpenSans-Semibold" -> GoogleFont.`Open Sans`.`600`,
    "OpenSans-Bold" -> GoogleFont.`Open Sans`.`700`,
    "OpenSans-Extrabold" -> GoogleFont.`Open Sans`.`800`
  )

  def apply(name: String): Future[Font] = {
    val url = map.getOrElse(name, throw new RuntimeException(s"Unmapped font: [$name]"))
    OpenTypeFont.fromURL(url)
  }
}
