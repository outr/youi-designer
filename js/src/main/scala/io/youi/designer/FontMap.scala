package io.youi.designer

import io.youi.font.{Font, GoogleFont, GoogleFontWeight, OpenTypeFont}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FontMap {
  private lazy val instance = for {
    openSansRegular <- GoogleFont.`Open Sans`.regular.load()
    openSans600 <- GoogleFont.`Open Sans`.`600`.load()
    openSans700 <- GoogleFont.`Open Sans`.`700`.load()
    openSans800 <- GoogleFont.`Open Sans`.`800`.load()
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

class FontMap(map: Map[String, GoogleFontWeight]) {
  def apply(name: String): GoogleFontWeight = {
    map.getOrElse(name, throw new RuntimeException(s"Unmapped font: [$name]"))
  }
}