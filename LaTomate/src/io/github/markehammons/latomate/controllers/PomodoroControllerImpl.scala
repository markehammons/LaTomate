package io.github.markehammons.latomate.controllers

import com.jfoenix.controls.{JFXButton, JFXSpinner}
import io.github.markehammons.latomate.Time
import io.github.markehammons.latomate.models.RootModel.{
  RootModel,
  SpawnPomodoroModel
}
import io.github.markehammons.latomate.models.pomodoro
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.media.AudioClip

import scala.concurrent.duration.FiniteDuration

class PomodoroControllerImpl(rootModel: RootModel) extends PomodoroController {
  @FXML
  protected var timeRemaining: Label = _

  @FXML
  protected var pomodoroProgress: JFXSpinner = _

  @FXML
  protected var statusText: Label = _

  @FXML
  protected var snoozeButton: JFXButton = _

  @FXML
  protected var stopButton: JFXButton = _

  @FXML
  protected var startButton: JFXButton = _

  private var pomodoroModel: Option[pomodoro.Requestee] = None

  private val warningTone: AudioClip = new AudioClip(
    getClass.getClassLoader.getResource("Metronome.wav").toExternalForm)

  private val dingTone: AudioClip = new AudioClip(
    getClass.getClassLoader.getResource("TempleBell.wav").toExternalForm)

  @FXML
  protected[controllers] def stop() = {
    pomodoroModel.foreach(_ ! pomodoro.Stop)
  }

  @FXML
  protected[controllers] def snooze() = {
    pomodoroModel.foreach(_ ! pomodoro.Snooze)
  }

  @FXML
  protected[controllers] def start() = {
    pomodoroModel.foreach(_ ! pomodoro.Start)
  }

  @FXML
  protected[controllers] def initialize() = {
    rootModel ! SpawnPomodoroModel(this)
  }

  def setModel(tM: pomodoro.Requestee) =
    Platform.runLater(() => {
      pomodoroModel = Some(tM)
    })

  def disableSnooze() =
    Platform.runLater(() => {
      snoozeButton.setDisable(true)
    })

  def reenableSnooze() =
    Platform.runLater(() => {
      snoozeButton.setDisable(false)
    })

  def updateTimer(timeLeft: FiniteDuration, progress: Double) =
    Platform.runLater(() => {
      timeRemaining.setText(Time.prettyPrintMillis(timeLeft.toMillis))
      pomodoroProgress.setProgress(progress)
    })

  def startBreak(statusMessage: String, duration: FiniteDuration) =
    Platform.runLater(() => {
      snoozeButton.setVisible(false)
      snoozeButton.setManaged(false)

      stopButton.setVisible(true)
      stopButton.setManaged(true)

      startButton.setVisible(false)
      startButton.setManaged(false)

      statusText.setText(statusMessage)
      timeRemaining.setText(Time.prettyPrintMillis(duration.toMillis))
      pomodoroProgress.setProgress(1.0)
    })

  def startWork(statusMessage: String, duration: FiniteDuration): Unit =
    Platform.runLater(() => {
      snoozeButton.setDisable(false)
      snoozeButton.setVisible(true)
      snoozeButton.setManaged(true)

      stopButton.setVisible(true)
      stopButton.setManaged(true)

      startButton.setVisible(false)
      startButton.setManaged(false)

      statusText.setText(statusMessage)
      timeRemaining.setText(Time.prettyPrintMillis(duration.toMillis))
      pomodoroProgress.setProgress(1.0)
    })

  def stopTimer(statusMessage: String): Unit =
    Platform.runLater(() => {
      snoozeButton.setVisible(false)
      snoozeButton.setManaged(false)

      stopButton.setVisible(false)
      stopButton.setManaged(false)

      startButton.setVisible(true)
      startButton.setManaged(true)

      statusText.setText(statusMessage)
      timeRemaining.setText(Time.prettyPrintMillis(0))
      pomodoroProgress.setProgress(-0.0)
    })

  def periodCompleteNotification() =
    Platform.runLater(() => {
      dingTone.play()
    })

  def periodEndingNotification() =
    Platform.runLater(() => {
      warningTone.play()
    })
}
