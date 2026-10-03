/**
 * settings.scala
 *
 * The little bit of configuration the calculator keeps between inputs. The value
 * is immutable: every change produces a new copy that main threads through its
 * loop instead of mutating shared state. Everything here can be changed at
 * runtime with the ':' commands in main (see :settings).
 */
final case class Settings(
	visuMode: Boolean,  // draw lambda diagrams instead of solving the equation
	showSteps: Boolean, // walk through every single beta reduction step
	autoSteps: Boolean, // showSteps: wait stepDelay ms between steps, false -> wait for enter
	stepDelay: Int      // how long to wait between steps in auto mode (ms)
)

object settings {

	// the configuration the calculator starts with
	val default: Settings = Settings(visuMode = false, showSteps = false, autoSteps = true, stepDelay = 700)

	def onOff(b: Boolean): String = if (b) "on" else "off"

	// the current config, shown by :settings
	def all(s: Settings): String =
		"settings:\n" +
		"  :visu          lambda diagrams instead of solving   " + onOff(s.visuMode) + "\n" +
		"  :steps         show every beta reduction step       " + onOff(s.showSteps) + "\n" +
		"  :auto          step by itself, no key needed        " + onOff(s.autoSteps) + "\n" +
		"  :delay <ms>    wait between steps in auto mode      " + s.stepDelay + " ms\n" +
		"  :settings      show this"
}
