package io.youi.designer

import io.youi.font.{Font, GoogleFont, OpenTypeFont}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FontMap {
  private lazy val instance = for {
    openSansRegular <- OpenTypeFont.fromURL(GoogleFont.`Open Sans`.regular)
    openSans600 <- OpenTypeFont.fromURL(GoogleFont.`Open Sans`.`600`)
    openSans700 <- OpenTypeFont.fromURL(GoogleFont.`Open Sans`.`700`)
    openSans800 <- OpenTypeFont.fromURL(GoogleFont.`Open Sans`.`800`)
  } yield {
    new FontMap(Map(
      "OpenSans" -> openSansRegular,
      "OpenSans-Semibold" -> openSans600,
      "OpenSans-Bold" -> openSans700,
      "OpenSans-Extrabold" -> openSans800
    ))
  }

  def apply(): Future[FontMap] = instance
}

class FontMap(map: Map[String, Font]) {
  def apply(name: String): Font = {
    map.getOrElse(name, throw new RuntimeException(s"Unmapped font: [$name]"))
  }
}