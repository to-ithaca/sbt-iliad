
package object iliad {
  object activityKeys extends ActivityKeys
  object androidKeys extends AndroidKeys
  object layoutKeys extends LayoutKeys
  object proguardKeys extends ProguardKeys
  object taskKeys extends TaskKeys
  object testKeys extends TestKeys
  object allKeys
      extends ActivityKeys
      with AndroidKeys
      with LayoutKeys
      with ProguardKeys
      with TaskKeys
      with TestKeys

  object androidSDKSettings extends AndroidSDKSettings
  object layoutSettings extends LayoutSettings
  object proguardSettings extends ProguardSettings
  object allSettings extends AllSettings
}
