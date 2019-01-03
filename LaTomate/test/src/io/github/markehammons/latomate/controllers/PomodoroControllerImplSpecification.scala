package io.github.markehammons.latomate.controllers

import akka.actor.testkit.typed.scaladsl.TestInbox
import io.github.markehammons.latomate.models.RootModel.{RootVocabulary, SpawnPomodoroModel}
import io.github.markehammons.latomate.models.pomodoro
import org.scalatest.fixture.WordSpec
import org.scalatest.{BeforeAndAfterAll, DiagrammedAssertions, Outcome}

class PomodoroControllerImplSpecification
    extends WordSpec
    with DiagrammedAssertions
    with BeforeAndAfterAll {

  case class FixtureParam(rootMailbox: TestInbox[RootVocabulary], pomodoroMailbox: TestInbox[pomodoro.Request]) {
    val pomodoroControllerImpl = new PomodoroControllerImpl(rootMailbox.ref)

    pomodoroControllerImpl.setModel(pomodoroMailbox.ref)
  }
  override def withFixture(test: OneArgTest): Outcome = {
    val param = FixtureParam(TestInbox[RootVocabulary](), TestInbox[pomodoro.Request]())
    super.withFixture(test.toNoArgTest(param))
  }


  "An FXMLPomodoroControllerImpl" should {
    "request a timer model from the root model on start" in { p =>
      p.pomodoroControllerImpl.bootModel()

      p.rootMailbox.expectMessage(SpawnPomodoroModel(p.pomodoroControllerImpl))
    }

    "send a start message on start function activation" in { p =>
      p.pomodoroControllerImpl.bootModel()
      p.pomodoroControllerImpl.start()

      p.pomodoroMailbox.expectMessage(pomodoro.Start)
    }

    "send a snooze message on snooze function activation" in { p =>
      p.pomodoroControllerImpl.bootModel()
      p.pomodoroControllerImpl.snooze()

      p.pomodoroMailbox.expectMessage(pomodoro.Snooze)
    }

    "send a stop message on stop function activation" in { p =>
      p.pomodoroControllerImpl.bootModel()
      p.pomodoroControllerImpl.stop()

      p.pomodoroMailbox.expectMessage(pomodoro.Stop)
    }
  }

}
