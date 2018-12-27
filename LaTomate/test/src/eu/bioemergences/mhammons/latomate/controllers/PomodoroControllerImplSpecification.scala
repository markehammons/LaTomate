package eu.bioemergences.mhammons.latomate.controllers

import akka.actor.testkit.typed.scaladsl.TestInbox
import eu.bioemergences.mhammons.latomate.models.RootModel.{
  RootVocabulary,
  SpawnTimerModel
}
import eu.bioemergences.mhammons.latomate.models.PomodoroModel.{
  Snooze,
  Start,
  Stop,
  TimerVocab
}
import javafx.application.Platform
import org.scalatest.{BeforeAndAfterAll, DiagrammedAssertions, WordSpec}

class PomodoroControllerImplSpecification
    extends WordSpec
    with DiagrammedAssertions
    with BeforeAndAfterAll {

  val rootMailbox = TestInbox[RootVocabulary]()

  val timerMailbox = TestInbox[TimerVocab]()

  val pomodoroControllerImpl = new PomodoroControllerImpl(rootMailbox.ref)

  Platform.startup(() => ())

  pomodoroControllerImpl.setModel(timerMailbox.ref)

  Thread.sleep(150)

  "An FXMLPomodoroControllerImpl" should {
    "request a timer model from the root model on start" in {
      pomodoroControllerImpl.initialize()

      rootMailbox.expectMessage(SpawnTimerModel(pomodoroControllerImpl))
    }

    "send a start message on start function activation" in {
      pomodoroControllerImpl.start()

      timerMailbox.expectMessage(Start)
    }

    "send a snooze message on snooze function activation" in {
      pomodoroControllerImpl.snooze()

      timerMailbox.expectMessage(Snooze)
    }

    "send a stop message on stop function activation" in {
      pomodoroControllerImpl.stop()

      timerMailbox.expectMessage(Stop)
    }
  }

}
