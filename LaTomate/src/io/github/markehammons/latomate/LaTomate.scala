package io.github.markehammons.latomate

import akka.actor.typed.ActorSystem
import io.github.markehammons.latomate.controllers.PomodoroControllerImpl
import io.github.markehammons.latomate.models.RootModel
import javafx.application.{Application => JavaFXApplication}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Stage

class LaTomate extends JavaFXApplication {
  private val rootModel = ActorSystem(RootModel.init, "LaTomate-Root")

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("LaTomate - Pomodoro Timer")

    val icon =
      getClass.getClassLoader.getResource("icons8-tomato-48.png").toExternalForm

    val image = new Image(icon)

    primaryStage.getIcons.add(image)

    val loader = new FXMLLoader(getClass.getResource("/pomodoro.fxml"))
    val timerController = new PomodoroControllerImpl(rootModel)
    loader.setController(timerController)
    val vbox = loader.load[VBox]

    val scene = new Scene(vbox)
    primaryStage.setScene(scene)
    primaryStage.show()
  }

  override def stop() = {
    rootModel.terminate()
  }
}