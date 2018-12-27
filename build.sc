import $ivy.`com.markehammons::mill-profiler-module:0.0.1-SNAPSHOT`
import mill.define.Target
import mill.scalalib._
import mill.util.Loose
import mill._
import com.markehammons.mill_profiler._
import mill.scalalib.scalafmt.ScalafmtModule

object LaTomate extends ScalaModule with YourKitModule with ScalafmtModule {
  def scalaVersion = "2.12.8"

  //override def millSourcePath = super.millSourcePath / ammonite.ops.up

  override def disableJavaVersionCheck: Target[Boolean] = true

  override def scalacPluginIvyDeps = Agg(
    ivy"org.scalamacros:::paradise:2.1.1"
  )

  val circeVersion = "0.10.0"

  val circeDeps = Agg(
    "circe-core",
    "circe-generic",
    "circe-generic-extras",
    "circe-parser"
  ).map(name => ivy"io.circe::$name:$circeVersion")

  val sttpVersion = "1.5.1"

  val sttpDeps = Agg(
    "core",
    "akka-http-backend"
  ).map(name => ivy"com.softwaremill.sttp::$name:$sttpVersion")

  val akkaVersion = "2.5.19"

  val akkaDeps = Agg(
    "akka-stream",
    "akka-actor",
    "akka-actor-typed"
  ).map(name => ivy"com.typesafe.akka::$name:$akkaVersion")

  val javafxVersion = "11.0.1"

  val javafxDeps = Agg(
    "javafx-base",
    "javafx-controls",
    "javafx-fxml",
    "javafx-graphics",
    "javafx-media"
  ).map(name => ivy"org.openjfx:$name:$javafxVersion")

  override def ivyDeps = javafxDeps ++ akkaDeps ++ sttpDeps ++ circeDeps ++ Agg(
    ivy"com.jfoenix:jfoenix:9.0.8"
  )

  override def compileIvyDeps: Target[Loose.Agg[Dep]] = Agg(
    ivy"com.lihaoyi::mill-scalalib:0.3.5"
  )

  override def scalacOptions: Target[Seq[String]] = T {
    Seq(
      "-unchecked",
      "-deprecation",
      "-encoding",
      "utf-8",
      "-explaintypes",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint"
    ) ++ super.scalacOptions()
  }

  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.5",
      ivy"org.scalamock::scalamock:4.1.0",
      ivy"com.typesafe.akka::akka-actor-testkit-typed:$akkaVersion",
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}
