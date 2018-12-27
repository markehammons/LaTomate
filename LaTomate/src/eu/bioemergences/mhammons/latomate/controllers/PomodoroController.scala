package eu.bioemergences.mhammons.latomate.controllers

import eu.bioemergences.mhammons.latomate.models.PomodoroModel.PomodoroModelRef

import scala.concurrent.duration.FiniteDuration

trait PomodoroController {
  def setModel(tM: PomodoroModelRef): Unit
  def disableSnooze(): Unit
  def updateTimer(timeMillis: Long, progress: Double): Unit
  def startBreak(statusMessage: String, duration: FiniteDuration): Unit
  def startWork(statusMessage: String, duration: FiniteDuration): Unit
  def stopTimer(statusMessage: String): Unit
  def periodCompleteNotification(): Unit
  def periodEndingNotification(): Unit
}
