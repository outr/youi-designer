package io.youi.designer

import io.youi.communication.{Communication, server}
import io.youi.designer.model.Root

import scala.concurrent.Future

trait DesignerCommunication extends Communication {
  @server def saveImage(psdFileName: String, fileName: String, dataURL: String): Future[Unit]

  @server def saveImport(psdFileName: String, layer: Root): Future[Unit]

  @server def listConversions(): Future[List[String]]

  @server def mergeConversions(directories: List[String]): Future[Unit]

  @server def loadScreen(screenName: String): Future[Option[Root]]
}