package iliad
package android
package std

import sbt._

import scala.language.implicitConversions

/** Conversions of sbt Logger to Android Loggers */
trait LoggerInstances {
  implicit def loggerOps(log: Logger): LoggerOps = new LoggerOps(log)
}

final class LoggerOps(log: Logger) {
  def android : AndroidLogger = new AndroidLogger(log)
  def errorReporter : AndroidErrorReporter = new AndroidErrorReporter(log)
  def progressIndicator : AndroidProgressIndicator = new AndroidProgressIndicator(log)
  def outputHandler: AndroidProcessOutputHandler = new AndroidProcessOutputHandler(log)
  def test: TestLogger = new TestLogger(log)
  def run: RunShellLogger = new RunShellLogger(log)
}


import com.android.utils.ILogger

final class AndroidLogger(log: Logger) extends ILogger {
  def verbose(s: String, objects: AnyRef*): Unit = log.debug(String.format(s, objects: _*))
  def warning(s: String, objects: AnyRef*): Unit = log.warn(String.format(s, objects: _*))
  def error(throwable: Throwable, s: String, objects: AnyRef*): Unit = log.error(String.format(s, objects: _*))
  def info(s: String, objects: AnyRef*): Unit = log.info(String.format(s, objects: _*))
}

import com.android.builder.core.ErrorReporter
import com.android.ide.common.blame.Message
import com.android.ide.common.blame.Message.Kind
import com.android.builder.model.SyncIssue
import com.android.build.gradle.internal.model.SyncIssueImpl

final class AndroidErrorReporter(log: Logger) extends ErrorReporter(ErrorReporter.EvaluationMode.STANDARD) {

  override def receiveMessage(message: Message) = message.getKind match {
    case Kind.ERROR => log.error(message.toString)
    case Kind.WARNING => log.warn(message.toString)
    case Kind.INFO => log.info(message.toString)
    case Kind.STATISTICS => log.debug(message.toString)
    case Kind.UNKNOWN => log.debug(message.toString)
    case Kind.SIMPLE => log.info(message.toString)
  }

  override def handleSyncIssue(data: String, `type`: Int, severity: Int, msg: String) = {
    severity match {
      case SyncIssue.SEVERITY_ERROR => log.error(s"android sync: data=$data, type=${`type`}, msg=$msg")
      case SyncIssue.SEVERITY_WARNING => log.warn(s"android sync: data=$data, type=${`type`}, msg=$msg")
      case _ => log.debug(s"android sync: severity=$severity data=$data, type=${`type`}, msg=$msg")
    }
    new SyncIssueImpl(`type`, severity, data, msg)
  }
}

import com.android.repository.api.ProgressIndicatorAdapter

final class AndroidProgressIndicator(log: Logger) extends ProgressIndicatorAdapter {

  private def trace(e: Throwable): Unit = Option(e).foreach(e => log.trace(e))

  override def logError(s: String, e: Throwable) = {
    log.error(s)
    trace(e)
  }
  override def logWarning(s: String, e: Throwable) = {
    log.warn(s)
    trace(e)
  }
  override def logInfo(s: String) = log.debug(s)
}


import com.android.ide.common.process.{BaseProcessOutputHandler, ProcessOutput}
import BaseProcessOutputHandler.BaseProcessOutput

final class AndroidProcessOutputHandler(log: Logger) extends BaseProcessOutputHandler {

  private def asOpt(s: String): Option[String] = Option(s).filter(_.trim.nonEmpty)

  override def handleOutput(processOutput: ProcessOutput) = {
    val baseOutput = processOutput.asInstanceOf[BaseProcessOutput]
    asOpt(baseOutput.getStandardOutputAsString).foreach(s => log.debug(s))
    asOpt(baseOutput.getErrorOutputAsString).foreach(s => log.error(s))
  }
}

import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.TestIdentifier

final class TestLogger(log: Logger) extends ITestRunListener {


  type TestMetrics = java.util.Map[String, String]

  override def testRunStarted(name: String, count: Int) {
    log.info("testing %s (tests: %d)" format(name, count))
  }

  override def testStarted(id: TestIdentifier) {
    log.info(" - " + id.getTestName)
  }

  override def testEnded(id: TestIdentifier, metrics: TestMetrics) {
    log.debug("finished: %s (%s)" format(id.getTestName, metrics))
  }

  override def testFailed(id: TestIdentifier, m: String) {
    log.error("failed: %s\n%s" format(id.getTestName, m))
  }

  override def testRunFailed(msg: String) {
    log.error("Testing failed: " + msg)
  }

  override def testRunStopped(elapsed: Long) {
    log.info("test run stopped (%dms)" format elapsed)
  }

  override def testRunEnded(elapsed: Long, metrics: TestMetrics) {
    log.info("test run finished (%dms)" format elapsed)
    log.debug("run end metrics: " + metrics)
  }

  override def testAssumptionFailure(id: TestIdentifier, m: String) {
    log.error("assumption failed: %s\n%s" format(id.getTestName, m))
  }

  override def testIgnored(test: TestIdentifier) {
    log.warn("ignored: %s" format test.getTestName)
  }
}


import com.android.ddmlib.IShellOutputReceiver

final class RunShellLogger(log: Logger) extends IShellOutputReceiver {

  private val b = new StringBuilder

  def logMessage(msg: String): Unit = log.info(msg)

  override def addOutput(data: Array[Byte], off: Int, len: Int) = {
    b.append(new String(data, off, len))
    val lastNL = b.lastIndexOf("\n")
    if (lastNL != -1) {
      b.mkString.split("\\n") foreach logMessage
      b.delete(0, lastNL + 1)
    }
  }

  override def flush() {
    b.mkString.split("\\n").filterNot(_.isEmpty) foreach logMessage
    b.clear()
  }

  override def isCancelled = false
}
