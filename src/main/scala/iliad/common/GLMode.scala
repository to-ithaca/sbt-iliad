package iliad
package common

sealed abstract class GLMode(val runner: String)
object GLMode {
  case object BASIC extends GLMode("def glRunner: _root_.iliad.gl.GLRunner = _root_.iliad.gl.GLBasicRunner")
  case object DEBUG extends GLMode("def glRunner: _root_.iliad.gl.GLRunner = _root_.iliad.gl.GLDebugLogRunner")
}
