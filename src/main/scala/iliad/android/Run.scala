package iliad
package android

import com.android.ddmlib.IDevice

import sbt._
import Keys._

import scalaz.concurrent.{Task => SZTask}

import iliad.android.std.logger._

/** Runs the activity on all connected devices */
object Run {
  import iliad.common.commonKeys._
  import taskKeys._
  import androidKeys._

  private def runTask(log: Logger, targetPackage: String, name: String, deviceStream: SZTask[List[IDevice]]) = {
    val receiver = log.run
    val command = s"am start $targetPackage/.$name"

    val runStream = deviceStream.map { devices =>
      devices.foreach { d =>
        log.info("Starting to run on device.")
        d.executeShellCommand(command, receiver)
        log.info("Finished running on device.")
      }
    }

    log.info(s"Running command $command")

    runStream.run
  }

  /** Runs the activity on all connected online devices
    *
    * Runs the activity specified by [[activityName]] in [[targetPackage]].
    * Runs on all devices in [[deviceStream]]
    */
  def apply() = Def.task{
    install.value
    runTask(streams.value.log, targetPackage.value, generatedAppName.value, deviceStream.value)
  }
}
