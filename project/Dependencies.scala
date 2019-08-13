import sbt._

object Dependencies {

  object Versions {
    val cats         = "1.6.0"
    val catsEffect   = "1.3.1"
    val monix        = "3.0.0-RC2"
    val logback      = "1.2.3"
    val scalaLogging = "3.9.2"

    // Test
    val scalaTest  = "3.0.7"
    val scalaCheck = "1.14.0"

    // Compiler
    val kindProjector    = "0.10.2"
    val betterMonadicFor = "0.3.0"
  }

  object Libraries {
    lazy val cats         = "org.typelevel"              %% "cats-core"      % Versions.cats
    lazy val catsKernel   = "org.typelevel"              %% "cats-kernel"    % Versions.cats
    lazy val catsEffect   = "org.typelevel"              %% "cats-effect"    % Versions.catsEffect
    lazy val monixEval    = "io.monix"                   %% "monix-eval"     % Versions.monix
    lazy val logback      = "ch.qos.logback"             % "logback-classic" % Versions.logback
    lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging"  % Versions.scalaLogging

    // Test
    lazy val scalaTest  = "org.scalatest"  %% "scalatest"  % Versions.scalaTest
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck

    // Compiler
    lazy val kindProjector    = "org.typelevel" %% "kind-projector"     % Versions.kindProjector
    lazy val betterMonadicFor = "com.olegpy"    %% "better-monadic-for" % Versions.betterMonadicFor
  }

}
