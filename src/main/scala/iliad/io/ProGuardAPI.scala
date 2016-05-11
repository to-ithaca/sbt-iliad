package iliad.io

import proguard.Configuration

/** Simplified API for [[proguard.ProGuard]] */
object ProGuardAPI {
  def execute(config: Configuration): Unit = {
    new proguard.ProGuard(config).execute()
  }
}