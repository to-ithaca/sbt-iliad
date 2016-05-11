package iliad

import com.android.ddmlib.IDevice

import scalaz.concurrent.{Task => SZTask}

import sbt.Keys._
import sbt._
import scala.util.{Failure, Try, Success}


/** Installs apk onto all connected devices */
object Install {
  import layoutKeys._
  import taskKeys._

  private def installToDevice(log: Logger, apk: File)(device: IDevice) {
    val reinstall = true
    Try(device.installPackage(apk.getAbsolutePath, reinstall)) match {
      case Failure(err) =>
        sys.error("Install failed: " + err.getMessage)
      case Success(_) =>
        log.info("Install successful.")
    }
  }

  private def runTask(log: Logger, apk: File, deviceStream: SZTask[List[IDevice]]) = {
    val installStream = deviceStream.map {
      case Nil => sys.error("No online devices connected.")
      case ds =>
        log.info(s"Found ${ds.length} devices.")
        log.info(s"Installing apk ${apk.absolutePath} to each device.")
        ds.foreach(installToDevice(log, apk))
        log.info("Finished installing.")
    }
    installStream.run
  }

  /** Installs apk onto all connected devices
    *
    * Runs after [[buildApk]].
    * Uses apk located at [[apkOut]].
    * Uses devices found in [[deviceStream]].
    */
  def apply() = Def.task{
    buildApk.value
    runTask(streams.value.log, apkOut.value, deviceStream.value)
  }
}