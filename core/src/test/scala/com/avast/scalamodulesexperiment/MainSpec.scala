package com.avast.scalamodulesexperiment

import cats.effect._
import org.scalatest.{ FlatSpec, Matchers }

class MainSpec extends FlatSpec with Matchers {
  "Main" should "run a println" in {
    Main.run(List.empty[String]).unsafeRunSync shouldBe ExitCode.Success
  }
}
