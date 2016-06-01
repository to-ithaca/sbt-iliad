package iliad
package android

import iliad.android.io.{SbtJavaProcessExecutor, BuilderAPI}
import iliad.android.std.logger._

import scala.collection.JavaConverters._

import com.android.builder.core.{LibraryRequest, AndroidBuilder}
import com.android.builder.sdk.DefaultSdkLoader
import com.android.ide.common.process.DefaultProcessExecutor
import com.android.sdklib.repositoryv2.AndroidSdkHandler
import sbt._
import Keys._


/** Creates an Android Builder */
object Builder {
  import iliad.android.androidKeys._

  private def runTask(log: Logger, name: String, androidHome: File, targetPlatform: String): BuilderAPI = {
    log.info("Creating Android Builder")
    val verboseExec = false
    val builder = new AndroidBuilder(name, "sbt-iliad", new DefaultProcessExecutor(log.android),
      SbtJavaProcessExecutor, log.errorReporter, log.android, verboseExec)

    val allowPreview = false
    val manager = AndroidSdkHandler.getInstance(androidHome)
    val buildTool = manager.getLatestBuildTool(log.progressIndicator, allowPreview)
    val revision = buildTool.getRevision
    log.debug(s"Getting target info for build tool revision $revision")

    val loader = DefaultSdkLoader.getLoader(manager.getLocation)
    val sdkInfo = loader.getSdkInfo(log.android)
    val targetInfo = loader.getTargetInfo(targetPlatform, revision, log.android)
    val libraryRequests = List.empty[LibraryRequest].asJava
    builder.setTargetInfo(sdkInfo, targetInfo, libraryRequests)

    log.info("Created Android Builder")
    new BuilderAPI(builder)
  }

  /** Instantiates an AndroidBuilder, with a simplified BuilderAPI
    *
    * Created using the build tools located at [[androidHome]]
    */
  def apply() = Def.task {
    runTask(streams.value.log, name.value, androidHome.value, targetPlatform.value)
  }
}

