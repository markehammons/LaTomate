package io.github.markehammons.latomate.controllers

import akka.actor.testkit.typed.scaladsl.TestInbox
import io.github.markehammons.latomate.models.RootModel.{RootVocabulary, SpawnPomodoroModel}
import io.github.markehammons.latomate.models.pomodoro
import io.github.markehammons.latomate.tags.GUI
import javafx.application.Platform
import org.scalatest.{BeforeAndAfterAll, DiagrammedAssertions, WordSpec}

class PomodoroControllerImplSpecification
    extends WordSpec
    with DiagrammedAssertions
    with BeforeAndAfterAll {

  val rootMailbox = TestInbox[RootVocabulary]()

  val pomodoroMailbox = TestInbox[pomodoro.Request]()

  val pomodoroControllerImpl = new PomodoroControllerImpl(rootMailbox.ref)

  Platform.startup(() => ())

  pomodoroControllerImpl.setModel(pomodoroMailbox.ref)

  Thread.sleep(150)

  "An FXMLPomodoroControllerImpl" should {
    "request a timer model from the root model on start" taggedAs GUI in {
      pomodoroControllerImpl.initialize()

      rootMailbox.expectMessage(SpawnPomodoroModel(pomodoroControllerImpl))
    }

    "send a start message on start function activation" taggedAs GUI in {
      pomodoroControllerImpl.start()

      pomodoroMailbox.expectMessage(pomodoro.Start)
    }

    "send a snooze message on snooze function activation" taggedAs GUI in {
      pomodoroControllerImpl.snooze()

      pomodoroMailbox.expectMessage(pomodoro.Snooze)
    }

    "send a stop message on stop function activation" taggedAs GUI in {
      pomodoroControllerImpl.stop()

      pomodoroMailbox.expectMessage(pomodoro.Stop)
    }
  }

}
