package iliad

import sbt._

/** Builds and installs an Android project onto connected devices */
object IliadPlugin extends AutoPlugin {

  /** Values are imported into the users sbt file automatically */
  object autoImport {

    /** Android target platform - required setting */
    val targetPlatform = androidKeys.targetPlatform

    /** Minimum Android SDK version - required setting */
    val minSdkVersion = androidKeys.minSdkVersion

    /** Package containing Android Activity - required setting */
    val targetPackage = androidKeys.targetPackage

    /** Name of Activity class to use - required setting */
    val activityName = androidKeys.activityName

    /** Package containing Android instrumentation tests - required setting */
    val testPackage = testKeys.testPackage

    /** If true, skips proguard obfuscation.  Default is false. */
    val skipProguard = proguardKeys.skipProguard

    /** Seq of aars to include as dependencies - required setting */
    val supportAars = androidKeys.supportAars

    /** All settings to include */
    val iliadSettings = allSettings.settings
  }
}
