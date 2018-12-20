package eu.bioemergences.mhammons.latomate.controllers

import eu.bioemergences.mhammons.latomate.models.TimerModel.TimerModel

trait TimerController {
  def setModel(tM: TimerModel): Unit
  def disableSnooze(): Unit
  def updateTimer(timeMillis: Long, progress: Double): Unit
  def resetTimer(): Unit
  def startBreak(statusMessage: String): Unit
  def startWork(statusMessage: String): Unit
  def stopTimer(statusMessage: String): Unit
  def periodCompleteNotification(message: String): Unit
  def periodEndingNotification(message: String): Unit
}
