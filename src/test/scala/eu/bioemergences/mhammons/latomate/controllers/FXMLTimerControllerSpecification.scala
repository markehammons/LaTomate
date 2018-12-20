package eu.bioemergences.mhammons.latomate.controllers

import akka.actor.testkit.typed.scaladsl.TestInbox
import eu.bioemergences.mhammons.latomate.models.RootModel.{
  RootVocabulary,
  SpawnTimerModel
}
import eu.bioemergences.mhammons.latomate.models.TimerModel.{
  Snooze,
  Start,
  Stop,
  TimerVocab
}
import javafx.application.Platform
import org.scalatest.{BeforeAndAfterAll, DiagrammedAssertions, WordSpec}

class FXMLTimerControllerSpecification
    extends WordSpec
    with DiagrammedAssertions
    with BeforeAndAfterAll {

  val rootMailbox = TestInbox[RootVocabulary]()

  val timerMailbox = TestInbox[TimerVocab]()

  val fxmlTimerController = new FXMLTimerController(rootMailbox.ref)

  Platform.startup(() => ())

  fxmlTimerController.setModel(timerMailbox.ref)

  "An FXMLTimerController" should {
    "request a timer model from the root model on start" in {
      fxmlTimerController.initialize()

      rootMailbox.expectMessage(SpawnTimerModel(fxmlTimerController))
    }

    "send a start message on start function activation" in {
      fxmlTimerController.start()

      timerMailbox.expectMessage(Start)
    }

    "send a snooze message on snooze function activation" in {
      fxmlTimerController.snooze()

      timerMailbox.expectMessage(Snooze)
    }

    "send a stop message on stop function activation" in {
      fxmlTimerController.stop()

      timerMailbox.expectMessage(Stop)
    }
  }

}
