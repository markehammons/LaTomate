import $ivy.`com.markehammons::mill-profiler-module:0.0.1-SNAPSHOT`


import mill.define.Target
import mill.scalalib._
import mill.util.Loose
import mill._
import mill.modules.Jvm
import mill.scalalib.JavaModule
import mill.eval.Result
import com.markehammons.mill_profiler._


object latomate extends SbtModule with YourKitModule {
  def scalaVersion = "2.12.8"

  override def millSourcePath = super.millSourcePath / ammonite.ops.up

  //override def yourKitHome = T { os.home / "bin" / "YourKit-JavaProfiler-2018.04" }

  override def disableJavaVersionCheck: Target[Boolean] = true

  override def scalacPluginIvyDeps = Agg(
    ivy"org.scalamacros:::paradise:2.1.1"
  )

  val circeVersion = "0.10.0"

  val circeDeps = Agg(
    ivy"io.circe::circe-core:$circeVersion",
    ivy"io.circe::circe-generic:$circeVersion",
    ivy"io.circe::circe-generic-extras:$circeVersion",
    ivy"io.circe::circe-parser:$circeVersion"
  )

  val sttpVersion = "1.5.1"

  val sttpDeps = Agg(
    ivy"com.softwaremill.sttp::core:$sttpVersion",
    ivy"com.softwaremill.sttp::akka-http-backend:$sttpVersion"
  )

  val akkaVersion = "2.5.19"

  val akkaDeps = Agg(
    ivy"com.typesafe.akka::akka-stream:$akkaVersion",
    ivy"com.typesafe.akka::akka-actor:$akkaVersion",
    ivy"com.typesafe.akka::akka-actor-typed:$akkaVersion"
  )

  val javafxVersion = "11.0.1"

  val javafxDeps = Agg(
    ivy"org.openjfx:javafx-base:$javafxVersion",
    ivy"org.openjfx:javafx-controls:$javafxVersion",
    ivy"org.openjfx:javafx-fxml:$javafxVersion",
    ivy"org.openjfx:javafx-graphics:$javafxVersion",
    ivy"org.openjfx:javafx-media:$javafxVersion"
  )

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
      "-encoding", "utf-8",
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