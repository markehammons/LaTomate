package eu.bioemergences.mhammons.latomate

object Time {
  def prettyPrintMillis(millis: Long) = {
    s"${millis / 60 / 1000}m ${millis / 1000 % 60}s"
  }
}
