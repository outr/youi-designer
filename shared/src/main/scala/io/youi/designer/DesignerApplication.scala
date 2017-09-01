package io.youi.designer

import io.youi.app.{CommunicationManager, YouIApplication}

trait DesignerApplication extends YouIApplication {
  val communication: CommunicationManager[DesignerCommunication] = connectivity.communication[DesignerCommunication]
}