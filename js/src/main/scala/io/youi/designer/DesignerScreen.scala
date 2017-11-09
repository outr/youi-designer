package io.youi.designer

import io.youi.app.screen.{PathActivation, UIScreen}

trait DesignerScreen extends UIScreen with PathActivation {
  def communication: DesignerCommunication = ClientDesignerApplication.communication(ClientDesignerApplication.clientConnectivity(ClientDesignerApplication.connectivity).connection)

  override lazy val path: String = s"/${getClass.getSimpleName.toLowerCase.replace("screen", "")}"
}