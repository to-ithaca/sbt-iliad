package iliad
package android

import iliad.io.IOReader

import sbt._
import sbt.Keys._

import cats._
import cats.implicits._
import cats.data._

object Aars {
  import layoutKeys._
  import androidKeys._

  private def zipPairs(sources: List[File], targetDir: File): List[(File, File)] = sources.map( src => src -> targetDir / s"${src.name}")

  private def renameJarIn(directory: File, name: String): Reader[IO.type, Unit] = {
    val jar = (directory ** "*.jar").get.head
    val renamedJar = jar.getParentFile / name
    IOReader.rename(jar, renamedJar)
  }

  private def ioOperation(zipPairs: List[(File, File)]): Reader[IO.type, Unit] = {
    val ops = zipPairs.map {
      case (s, d) => for {
        _ <- IOReader.unzip(s, d)
        _ <- renameJarIn(d, s"${d.name}.jar")
        } yield ()
    }

    val traversable = implicitly[Traverse[List]]
    traversable.sequence[Reader[IO.type, ?], Unit](ops).map(_ => ())
  }

  private def runTask(log: Logger, supportRepository: File,  supportAars: Seq[String], targetDir: File): Seq[Attributed[File]] = {
    val sources = supportAars.map(suffix => supportRepository / suffix).toList
    val pairs = zipPairs(sources, targetDir)
    val ioOp = ioOperation(pairs)

    log.info(s"Using aar target directory ${targetDir.absolutePath}")
    log.info(s"Unzipping ${supportAars.size} aars:")
    pairs.foreach {
      case (s, d) => log.info(s"src ${s.absolutePath} -> dest ${d.absolutePath}")
    }

    ioOp.run(IO)
    log.info("Finished unzipping aars")

    val extractedJars = (targetDir ** "*.jar").get
    extractedJars.map(Attributed.blank)
  }

  def apply() = Def.task {
    runTask(streams.value.log,supportRepository.value, supportAars.value, aarOut.value)
  }
}
