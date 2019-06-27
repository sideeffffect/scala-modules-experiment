import Dependencies.Libraries

name := """scala-modules-experiment"""

organization in ThisBuild := "com.avast"

crossScalaVersions in ThisBuild := Seq("2.11.12", scalaVersion.value, "2.13.0")

lazy val warts = Warts.allBut(
  Wart.Any,
  Wart.ArrayEquals,
  Wart.Nothing,
  Wart.Product,
  Wart.Serializable,
  Wart.Overloading,
  Wart.NonUnitStatements,
  Wart.ImplicitConversion,
  Wart.PublicInference,
  Wart.ImplicitParameter
)

lazy val commonSettings = Seq(
  organizationName := "com.avast",
  wartremoverErrors in (Compile, compile) := warts,
  wartremoverErrors in (Test, compile) := warts,
  libraryDependencies ++= Seq(
    Libraries.cats,
    Libraries.catsEffect,
    Libraries.monix,
    Libraries.scalaTest  % Test,
    Libraries.scalaCheck % Test,
    compilerPlugin(Libraries.kindProjector),
    compilerPlugin(Libraries.betterMonadicFor)
  )
)

lazy val testSettings = Seq(
  fork in Test := true,
  parallelExecution in Test := false
)

lazy val `scala-modules-experiment-root` =
  (project in file("."))
    .aggregate(`scala-modules-experiment-core`)

lazy val `scala-modules-experiment-core` = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(testSettings)
