package iliad


import sbt._

/** Builds and installs an Iliad application onto specified platforms */
object IliadPlugin extends AutoPlugin {

  /** Values are imported into the user's sbt file automatically */
  object autoImport 
      extends CommonAutoImport 
      with AndroidAutoImport 
      with X11AutoImport

  /** Values for defining the Iliad application */
  trait CommonAutoImport {
    import iliad.common._

    /** Name of the application - required setting */
    val appName = commonKeys.appName

    /** Target package for the generated app - required setting */
    val targetPackage = commonKeys.targetPackage

    /** Name of the generated app - required setting */
    val generatedAppName = commonKeys.generatedAppName
  }

  /** Values for an Android target */
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

  /** Values for an X11 (Linux / Mac) target */
  trait X11AutoImport {
    import x11._

    /**Width of the app window */
    val width = viewKeys.width

    /** Height of the app window */
    val height = viewKeys.height

    /**All settings to include */
    val iliadX11Settings = allSettings.settings
  }
}
