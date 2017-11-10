package io.youi.designer.model

import io.youi.Horizontal

sealed trait Entry {
  def id: String
}

case class Image(var id: String,
                 var fileName: String,
                 x: Double,
                 y: Double,
                 opacity: Double) extends Entry

case class Group(var id: String, children: List[Entry]) extends Entry

case class Text(var id: String,
                text: String,
                font: String,
                size: Double,
                color: Long,
                alignment: Horizontal,
                x: Double,
                y: Double,
                opacity: Double) extends Entry