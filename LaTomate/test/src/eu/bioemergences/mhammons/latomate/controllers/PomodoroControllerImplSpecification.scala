package eu.bioemergences.mhammons.latomate.controllers

import akka.actor.testkit.typed.scaladsl.TestInbox
import eu.bioemergences.mhammons.latomate.models.RootModel.{
  RootVocabulary,
  SpawnPomodoroModel
}
import eu.bioemergences.mhammons.latomate.models.PomodoroModel.{
  Snooze,
  Start,
  Stop,
  PomodoroVocab
}
import javafx.application.Platform
import org.scalatest.{BeforeAndAfterAll, DiagrammedAssertions, WordSpec}

class PomodoroControllerImplSpecification
    extends WordSpec
    with DiagrammedAssertions
    with BeforeAndAfterAll {

  val rootMailbox = TestInbox[RootVocabulary]()

  val pomodoroMailbox = TestInbox[PomodoroVocab]()

  val pomodoroControllerImpl = new PomodoroControllerImpl(rootMailbox.ref)

  Platform.startup(() => ())

  pomodoroControllerImpl.setModel(pomodoroMailbox.ref)

  Thread.sleep(150)

  "An FXMLPomodoroControllerImpl" should {
    "request a timer model from the root model on start" in {
      pomodoroControllerImpl.initialize()

      rootMailbox.expectMessage(SpawnPomodoroModel(pomodoroControllerImpl))
    }

    "send a start message on start function activation" in {
      pomodoroControllerImpl.start()

      pomodoroMailbox.expectMessage(Start)
    }

    "send a snooze message on snooze function activation" in {
      pomodoroControllerImpl.snooze()

      pomodoroMailbox.expectMessage(Snooze)
    }

    "send a stop message on stop function activation" in {
      pomodoroControllerImpl.stop()

      pomodoroMailbox.expectMessage(Stop)
    }
  }

}
