package iliad

package object x11 {
  object viewKeys extends ViewKeys
  object layoutKeys extends LayoutKeys

  object allKeys 
      extends TaskKeys 
      with ViewKeys 
      with LayoutKeys

  object allSettings extends AllSettings
  object layoutSettings extends LayoutSettings
}
