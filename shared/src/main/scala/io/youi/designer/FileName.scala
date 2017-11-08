package io.youi.designer

case class FileName(name: String, extension: Option[String], increment: Int) {
  def deduplicate(f: String => Boolean): FileName = if (f(toString)) {
    copy(increment = increment + 1).deduplicate(f)
  } else {
    this
  }

  override def toString: String = {
    val b = new StringBuilder
    b.append(name)
    if (increment > 0) {
      b.append(s"_$increment")
    }
    extension.foreach(ext => b.append(s".$ext"))
    b.toString()
  }
}

object FileName {
  def apply(fileName: String): FileName = {
    val index = fileName.lastIndexOf('.')
    if (index == -1) {
      apply(fileName, None)
    } else {
      apply(fileName.substring(0, index), Some(fileName.substring(index + 1)))
    }
  }

  def apply(name: String, extension: Option[String]): FileName = {
    var parts = name.split("[ ._-]").toList.filterNot(_.trim.isEmpty)
    if (parts.headOption.exists(_.matches("[0-9]+"))) {
      parts = parts.tail
    }
    val increment = if (parts.last.matches("[0-9]+")) {
      val i = parts.last.toInt
      parts = parts.reverse.tail.reverse
      i
    } else {
      0
    }
    val prefix = if (parts.isEmpty) {
      "image"
    } else {
      parts.mkString("_").toLowerCase
    }
    FileName(prefix, extension.map(_.toLowerCase), increment)
  }
}