package eu.bioemergences.mhammons.latomate.controllers

import eu.bioemergences.mhammons.latomate.models._

import scala.concurrent.duration.FiniteDuration

trait PomodoroController {
  def setModel(tM: pomodoro.Requestee): Unit
  def disableSnooze(): Unit
  def updateTimer(timeLeft: FiniteDuration, progress: Double): Unit
  def startBreak(statusMessage: String, duration: FiniteDuration): Unit
  def startWork(statusMessage: String, duration: FiniteDuration): Unit
  def stopTimer(statusMessage: String): Unit
  def periodCompleteNotification(): Unit
  def periodEndingNotification(): Unit
}
