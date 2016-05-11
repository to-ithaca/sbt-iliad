package iliad

import com.android.ddmlib.{IDevice, DdmPreferences, IShellOutputReceiver}
import com.android.ddmlib.testrunner.{ITestRunListener, InstrumentationResultParser}

import scalaz.concurrent.{Task => SZTask}

import sbt._
import Keys._

/** Runs instrumentation tests */
import iliad.std.logger._

object AndroidTestTask {
  import testKeys._
  import taskKeys._

  private val testTimeout = 180000

  private def runTask(log: Logger, deviceStream: SZTask[List[IDevice]], testPackage: String) = {
    val testStream = deviceStream.map { devices =>
      log.info(s"running tests on ${devices.size} devices")
      devices.foreach { device =>

        val listener = TestListener(log)
        val receiver = new ShellLogger(listener, testPackage)
        val runner = "android.test.InstrumentationTestRunner"
        val command = s"am instrument -r -w $testPackage/$runner"
        log.info(s"executing command $command")

        val normalTimeout = DdmPreferences.getTimeOut
        DdmPreferences.setTimeOut(testTimeout)
        device.executeShellCommand(command, receiver)
        DdmPreferences.setTimeOut(normalTimeout)

        log.debug("instrument command executed")
        listener.error.foreach(e => sys.error(e))
      }
    }

    testStream.run
    log.info("Finished running tests")
  }


  /** Runs instrumentation tests
    *
    * Instrumentation tests are located in the androidTest directory.
    * Tests are run on all online connected devices in [[deviceStream]].
    */
  def apply() = Def.task {
    install.value
    runTask(streams.value.log, deviceStream.value, testPackage.value)
  }
}


/** Redirects shell output from device to test listener */
class ShellLogger[A](listener: TestListener, testPackage: String) extends IShellOutputReceiver {
  private val parser = new InstrumentationResultParser(testPackage, listener)

  private val b = new StringBuilder

  def log(msg: String): Unit = parser.processNewLines(Array(msg))

  override def addOutput(data: Array[Byte], off: Int, len: Int) = {
    b.append(new String(data, off, len))
    val lastNL = b.lastIndexOf("\n")
    if (lastNL != -1) {
      b.mkString.split("\\n") foreach log
      b.delete(0, lastNL + 1)
    }
  }

  override def flush() {
    b.mkString.split("\\n").filterNot(_.isEmpty) foreach log
    b.clear()
  }

  override def isCancelled = false
}
import com.android.ddmlib.testrunner.TestIdentifier

/** Records test results */
private case class TestListener(sbtLog: Logger) extends ITestRunListener {

  private val log = sbtLog.test

  private var _failures: Seq[TestIdentifier] = Seq.empty

  type TestMetrics = java.util.Map[String,String]
  override def testRunStarted(name: String, count: Int) = log.testRunStarted(name, count)
  override def testStarted(id: TestIdentifier)  = log.testStarted(id)
  override def testEnded(id: TestIdentifier, metrics: TestMetrics)  = log.testEnded(id, metrics)
  override def testRunFailed(msg: String) = log.testRunFailed(msg)
  override def testRunStopped(elapsed: Long) = log.testRunStopped(elapsed)
  override def testIgnored(test: TestIdentifier) = log.testIgnored(test)
  override def testRunEnded(elapsed: Long, metrics: TestMetrics) = log.testRunEnded(elapsed, metrics)

  override def testFailed(id: TestIdentifier, m: String) {
    log.testFailed(id, m)
    _failures = _failures :+ id
  }

  override def testAssumptionFailure(id: TestIdentifier, m: String) {
    log.testAssumptionFailure(id, m)
    _failures = _failures :+ id
  }

  def error: Option[String] = if(_failures.nonEmpty) {
    Some(s"""Tests failed: ${_failures.size}
             ${_failures.map(" - " + _).mkString("\n")}
          """)
  } else None
}
