package iliad.io

import cats._
import cats.data._
import cats.implicits._

import sbt._

/** Reader Monad for [[sbt.IO]] */
object IOReader {
  def createDirectory(file: File): Reader[IO.type, Unit] = Reader(_.createDirectory(file))
  def createDirectoryIfNotExists(file: File): Reader[IO.type, Unit] = Reader(io => if(!file.exists()) io.createDirectory(file))

  def copy(sources: Traversable[(File, File)], overwrite: Boolean): Reader[IO.type , Unit] = Reader(_.copy(sources, overwrite))
}