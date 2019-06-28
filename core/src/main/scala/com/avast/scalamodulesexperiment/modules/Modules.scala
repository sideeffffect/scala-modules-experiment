package com.avast.scalamodulesexperiment.modules

import scala.language.higherKinds

object Signatures {

  trait ConsoleWriter[F[_]] {
    def writeString(s: String): F[Unit]
    def writeChar(c: Char): F[Unit]
  }

  trait ConsoleReader[F[_]] {
    def readLine: F[String]
  }

  trait Greeter[F[_]] {
    def greet(name: String): F[Unit]
  }
}

object Modules {

  import Signatures._
  import cats.Monad
  import cats.implicits._
  import monix.eval.Task

  object MonixConsoleWriter {
    def create(): ConsoleWriter[Task] = new ConsoleWriter[Task] {
      def writeString(s: String): Task[Unit] = Task.delay { print(s) }
      def writeChar(c: Char): Task[Unit]     = Task.delay { print(c) }
    }
  }

  object MonixConsoleReader {
    def create[F[_]](): Task[ConsoleReader[Task]] =
      for {
        () <- Task.delay { println("initializing a MonixConsoleReader...") }
        r = new ConsoleReader[Task] {
          val readLine: Task[String] = Task.delay { scala.io.StdIn.readLine() }
        }
        () <- Task.delay { println("MonixConsoleReader initialized.") }
      } yield r
  }

  object ConsoleGreeter {
    def create[F[_]: Monad](consoleWriter: ConsoleWriter[F]): Greeter[F] =
      new Greeter[F] {
        override def greet(name: String): F[Unit] =
          for {
            () <- consoleWriter.writeString("hello ")
            () <- consoleWriter.writeString(name)
            () <- consoleWriter.writeChar('\n')
          } yield ()
      }
  }

  object Main {
    def run[F[_]: Monad](
        consoleWriter: ConsoleWriter[F],
        consoleReader: ConsoleReader[F],
        greeter: Greeter[F],
    ): F[Unit] =
      for {
        () <- consoleWriter.writeString("vots jor nejm?\n")
        name <- consoleReader.readLine
        () <- name match {
               case "banik" => consoleWriter.writeString("banik pyco\n")
               case _       => greeter.greet(name)
             }
      } yield ()
  }
}

object Tests {
  import Signatures._
  import Modules._
  import cats.Id
  import cats.implicits._

  object TestConsoleWriter extends ConsoleWriter[Id] {
    // YOLO (and this is just a test anyway...)
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var output = "" // scalastyle:off

    def writeString(s: String): Id[Unit] = output = output + s
    def writeChar(c: Char): Id[Unit]     = output = output + c.toString
  }

  object TestConsoleReader extends ConsoleReader[Id] {
    val readLine: Id[String] = "asdf"
  }

  private val TestGreeter = ConsoleGreeter.create(TestConsoleWriter)

  def main(args: Array[String]): Unit = {
    Main.run(TestConsoleWriter, TestConsoleReader, TestGreeter)
    assert(TestConsoleWriter.output === "vots jor nejm?\nhello asdf\n")
  }
}

object App extends monix.eval.TaskApp {

  import Modules._
  import cats.effect.ExitCode
  import cats.implicits._
  import monix.eval.Task

  private val monixConsoleWriter = MonixConsoleWriter.create()
  private val monixConsoleReader = MonixConsoleReader.create()
  private val monixGreeter       = ConsoleGreeter.create[Task](monixConsoleWriter)

  private val task: Task[Unit] = for {
    reader <- monixConsoleReader
    () <- Main.run(monixConsoleWriter, reader, monixGreeter)
  } yield ()

  override def run(args: List[String]): Task[ExitCode] =
    task >> Task.now(ExitCode.Success)
}

object EffectModules {

  import Signatures._
  import cats.effect.Resource
  import cats.effect.Sync
  import cats.implicits._

  object EffectConsoleWriter {
    def create[F[_]]()(implicit F: Sync[F]): ConsoleWriter[F] =
      new ConsoleWriter[F] {
        def writeString(s: String): F[Unit] = F.delay { print(s) }
        def writeChar(c: Char): F[Unit]     = F.delay { print(c) }
      }
  }

  object EffectConsoleReader {
    def create[F[_]]()(implicit F: Sync[F]): Resource[F, ConsoleReader[F]] =
      Resource {
        for {
          () <- F.delay { println("initializing a EffectConsoleReader...") }
          r = new ConsoleReader[F] {
            val readLine: F[String] = F.delay(scala.io.StdIn.readLine())
          }
          () <- F.delay { println("EffectConsoleReader initialized.") }
          free = F.delay { println("Finalizing a EffectConsoleReader.") }
        } yield (r, free)
      }
  }
}

object EffectMain extends monix.eval.TaskApp {

  import cats.effect.ExitCode
  import cats.implicits._
  import monix.eval.Task

  import EffectModules._
  import Modules.{ ConsoleGreeter, Main }

  private val effectConsoleWriter = EffectConsoleWriter.create()
  private val effectConsoleReader = EffectConsoleReader.create()
  private val effectGreeter       = ConsoleGreeter.create[Task](effectConsoleWriter)

  private val task: Task[Unit] = {
    effectConsoleReader.use { reader =>
      Main.run(effectConsoleWriter, reader, effectGreeter)
    }
  }

  override def run(args: List[String]): Task[ExitCode] =
    task >> Task.now(ExitCode.Success)
}
