package iliad


import sbt._

/** Builds and installs an Android project onto connected devices */
object IliadPlugin extends AutoPlugin {

  /** Values are imported into the user's sbt file automatically */
  object autoImport 
      extends CommonAutoImport 
      with AndroidAutoImport 
      with X11AutoImport

  trait CommonAutoImport {
    import iliad.common._

    /** Name of the application */
    val appName = commonKeys.appName

    /** Target package for the generated app */
    val targetPackage = commonKeys.targetPackage

    /** Name of the generated app */
    val generatedAppName = commonKeys.generatedAppName
  }

  trait AndroidAutoImport {
    import android._
    /** Android target platform - required setting */
    val targetPlatform = androidKeys.targetPlatform

    /** Minimum Android SDK version - required setting */
    val minSdkVersion = androidKeys.minSdkVersion

    /** Package containing Android instrumentation tests - required setting */
    val testPackage = testKeys.testPackage

    /** If true, skips proguard obfuscation.  Default is false. */
    val skipProguard = proguardKeys.skipProguard

    /** Seq of aars to include as dependencies - required setting */
    val supportAars = androidKeys.supportAars

    /** All settings to include */
    val iliadAndroidSettings = allSettings.settings
  }

  trait X11AutoImport {
    import x11._

    val width = viewKeys.width
    val height = viewKeys.height

    val iliadX11Settings = allSettings.settings
  }
}
