package iliad
package android

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.{IDevice, AndroidDebugBridge}

import sbt._
import Keys._

import scala.collection.{mutable => mu}
import scala.language.postfixOps

import scalaz.concurrent.{Task => SZTask}

/** Finds all connected devices using adb (Android Debug Bridge) */
object Devices {
  import iliad.android.androidKeys._

  import scala.concurrent.duration._
  private val timeout = 1 second

  private def startAdb(log: Logger) = {
    log.debug("Initialising adb.")
    val clientSupport = false
    AndroidDebugBridge.initIfNeeded(clientSupport)
  }

  private def createBridge(log: Logger, adb: File) = {
    val path = adb.absolutePath
    log.debug(s"Using adb $path")
    val forceNewBridge = false
    AndroidDebugBridge.createBridge(path, forceNewBridge)
  }

  private def runTask(log: Logger, adb: File): SZTask[List[IDevice]] = {
    startAdb(log)
    val bridge = createBridge(log, adb)
    val l = new DeviceListener(log, bridge)
    AndroidDebugBridge.addDeviceChangeListener(l)
    SZTask(l.devices.filter(_.isOnline)).after(timeout)
  }

  /** Returns a stream of connected online devices
    *
    * Uses adb.exe located at [[adb]]
    */
  def apply() = Def.task(runTask(streams.value.log, adb.value))
}

/** Listens to changes in connected devices */
private final class DeviceListener(log: Logger, adb: AndroidDebugBridge) extends IDeviceChangeListener {
  log.debug("Created device listener")
  private val _devices = mu.Set[IDevice]()

  def deviceChanged(d: IDevice, i: Int): Unit = {
    log.debug(s"Device ${d.getName} has changed.")
  }
  def deviceConnected(d: IDevice): Unit = {
    log.debug(s"Device ${d.getName} has connected.")
    _devices += d
  }
  def deviceDisconnected(d: IDevice): Unit = {
    log.debug(s"Device ${d.getName} has disconnected.")
    _devices -= d
  }
  def devices = (_devices ++ adb.getDevices.toSet).toList
}

