package com.avast.scalamodulesexperiment.zioinspired

import scala.language.higherKinds

object Algebra {

  trait ConsoleWriter[F[_]] {
    def consoleWriter: ConsoleWriter.Service[F]
  }

  object ConsoleWriter {
    trait Service[F[_]] {
      def writeString(s: String): F[Unit]
      def writeChar(c: Char): F[Unit]
    }
  }

  trait ConsoleReader[F[_]] {
    def consoleReader: ConsoleReader.Service[F]
  }

  object ConsoleReader {
    trait Service[F[_]] {
      def readLine: F[String]
    }
  }
}

object Programs {
  import Algebra._
  import cats.implicits._

  type MyTypicallyUsedServices[F[_]] = ConsoleWriter[F] with ConsoleReader[F]

  def greeter[F[_]: cats.Monad](services: ConsoleWriter[F])(name: String): F[Unit] = {
    import services._
    for {
      () <- consoleWriter.writeString("hello ")
      () <- consoleWriter.writeString(name)
      () <- consoleWriter.writeChar('\n')
    } yield ()
  }

  def myCoolProgram[F[_]: cats.Monad](services: MyTypicallyUsedServices[F]): F[Unit] = {
    val greeter_ = greeter(services) _ // doing this is totally optional, applying greeter to all its arguments at the place of call is OK
    import services._ // also not mandatory, but makes for a nicer reading
    for {
      () <- consoleWriter.writeString("vots jor nejm?\n")
      name <- consoleReader.readLine
      () <- name match {
             case "banik" => consoleWriter.writeString("banik pyco")
             case _       => greeter_(name)
           }
    } yield ()
  }
}

object Interpreters {
  import Algebra._
  import monix.eval.Task

  trait MonixConsoleWriter extends ConsoleWriter[Task] {
    val consoleWriter: ConsoleWriter.Service[Task] = new ConsoleWriter.Service[Task] {
      def writeString(s: String): Task[Unit] = Task.delay { print(s) }
      def writeChar(c: Char): Task[Unit]     = Task.delay { print(c) }
    }
  }

  object MonixConsoleWriter extends MonixConsoleWriter

  trait MonixConsoleReader extends ConsoleReader[Task] {
    val consoleReader: ConsoleReader.Service[Task] = new ConsoleReader.Service[Task] {
      val readLine: Task[String] = Task.delay { scala.io.StdIn.readLine() }
    }
  }

  object MonixConsoleReader extends MonixConsoleReader
}

object Tests {
  import Algebra._
  import Programs._
  import cats.Id
  import cats.implicits._

  trait TestConsoleWriter extends ConsoleWriter[Id] {
    // YOLO (and this is just a test anyway...)
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var output = "" // scalastyle:off

    val consoleWriter: ConsoleWriter.Service[Id] = new ConsoleWriter.Service[Id] {
      def writeString(s: String): Id[Unit] = output = output + s
      def writeChar(c: Char): Id[Unit]     = output = output + c.toString
    }
  }

  trait TestConsoleReader extends ConsoleReader[Id] {
    val consoleReader: ConsoleReader.Service[Id] = new ConsoleReader.Service[Id] {
      val readLine: Id[String] = "asdf"
    }
  }

  val TestServices = new TestConsoleWriter with TestConsoleReader

  def main(args: Array[String]): Unit = {
    myCoolProgram(TestServices)
    assert(TestServices.output === "vots jor nejm?\nhello asdf\n")
  }
}

object App extends monix.eval.TaskApp {

  import Interpreters._
  import Programs._
  import cats.effect.ExitCode
  import cats.implicits._
  import monix.eval.Task

  val MonixServices = new MonixConsoleWriter with MonixConsoleReader

  val task: Task[Unit] = myCoolProgram(MonixServices)

  override def run(args: List[String]): Task[ExitCode] = task >> Task.now(ExitCode.Success)
}

object EffectInterpreters {

  import Algebra._
  import cats.effect.Sync

  class EffectConsoleWriterService[F[_]](implicit F: Sync[F]) extends ConsoleWriter.Service[F] {
    def writeString(s: String): F[Unit] = F.delay { print(s) }
    def writeChar(c: Char): F[Unit]     = F.delay { print(c) }
  }

  class EffectConsoleReaderService[F[_]](implicit F: Sync[F]) extends ConsoleReader.Service[F] {
    val readLine: F[String] = F.delay { scala.io.StdIn.readLine() }
  }
}

class EffectApp[F[_]: cats.effect.Sync] {

  import Algebra._
  import EffectInterpreters._
  import Programs._

  object EffectServices extends ConsoleWriter[F] with ConsoleReader[F] {
    val consoleWriter: ConsoleWriter.Service[F] = new EffectConsoleWriterService[F]
    val consoleReader: ConsoleReader.Service[F] = new EffectConsoleReaderService[F]
  }

  val task: F[Unit] = myCoolProgram(EffectServices)
}

object EffectMain extends EffectApp[monix.eval.Task] with monix.eval.TaskApp {

  import cats.effect.ExitCode
  import cats.implicits._
  import monix.eval.Task

  override def run(args: List[String]): Task[ExitCode] = task >> Task.now(ExitCode.Success)
}
