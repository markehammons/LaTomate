package eu.bioemergences.mhammons.latomate.controllers

import eu.bioemergences.mhammons.latomate.models.TimerModel.TimerModel

import scala.concurrent.duration.FiniteDuration

trait TimerController {
  def setModel(tM: TimerModel): Unit
  def disableSnooze(): Unit
  def updateTimer(timeMillis: Long, progress: Double): Unit
  def startBreak(statusMessage: String, duration: FiniteDuration): Unit
  def startWork(statusMessage: String, duration: FiniteDuration): Unit
  def stopTimer(statusMessage: String): Unit
  def periodCompleteNotification(): Unit
  def periodEndingNotification(): Unit
}
