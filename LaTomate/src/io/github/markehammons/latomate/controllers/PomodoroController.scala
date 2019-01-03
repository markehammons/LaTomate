package io.github.markehammons.latomate.controllers

import io.github.markehammons.latomate.models._

import scala.concurrent.duration.FiniteDuration

trait PomodoroController {
  def setModel(tM: pomodoro.Requestee): Unit
  protected def bootModel(): Unit
  def disableSnooze(): Unit
  def updateTimer(timeLeft: FiniteDuration, progress: Double): Unit
  def startBreak(statusMessage: String, duration: FiniteDuration): Unit
  def startWork(statusMessage: String, duration: FiniteDuration): Unit
  def stopTimer(statusMessage: String): Unit
  def periodCompleteNotification(): Unit
  def periodEndingNotification(): Unit
}
