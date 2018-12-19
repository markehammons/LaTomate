package eu.bioemergences.mhammons.latomate

import akka.actor.typed.ActorSystem
import eu.bioemergences.mhammons.latomate.controllers.TimerController
import eu.bioemergences.mhammons.latomate.models.RootModel
import javafx.application.{Application => JavaFXApplication}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage

class LaTomate extends JavaFXApplication {
  private val rootModel = ActorSystem(RootModel.init, "LaTomate-Root")

  override def start(primaryStage: Stage): Unit = {
    val testMode = this.getParameters.getRaw.contains("testMode")
    primaryStage.setTitle("LaTomate - Pomodoro Timer")

    val loader = new FXMLLoader(getClass.getResource("/timer.fxml"))
    val timerController = new TimerController(rootModel)
    loader.setController(timerController)
    val vbox = loader.load[VBox]

    val scene = new Scene(vbox)
    primaryStage.setScene(scene)
    primaryStage.show()

    Thread.sleep(1000)
    if (testMode) {
      timerController.periodCompleteNotification("test")
      timerController.periodEndingNotification("test2")
    }
  }

  override def stop() = {
    rootModel.terminate()
  }
}

object LaTomate {
  def run(args: Array[String]) = {
    JavaFXApplication.launch(classOf[LaTomate], args.toSeq: _*)
  }
}
