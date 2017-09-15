package io.youi.designer.model

import io.youi.Horizontal

sealed trait Entry {
  def id: String
}

case class Image(id: String,
                 fileName: String,
                 x: Double,
                 y: Double,
                 width: Double,
                 height: Double,
                 opacity: Double) extends Entry

case class Group(id: String, children: List[Entry]) extends Entry

case class Text(id: String,
                text: String,
                font: String,
                size: Double,
                color: Long,
                alignment: Horizontal,
                x: Double,
                y: Double,
                width: Double,
                height: Double,
                opacity: Double) extends Entry