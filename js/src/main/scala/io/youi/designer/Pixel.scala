package io.youi.designer

case class Pixel(red: Int, green: Int, blue: Int, alpha: Int) {
  override def toString: String = s"Pixel(red: $red, green: $green, blue: $blue, alpha: $alpha)"
}
