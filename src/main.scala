//> using dep "org.jline:jline:4.4.6"

object main {

	def main(args: Array[String]): Unit = {
		val startSettings = parseArgs(args.toList, settings.default)
		val handle        = console.init()

		println("Calculator started, enter an equation to solve (empty line to quit):")
		println("type :settings for the settings, :visu to toggle the lambda diagrams")
		loop(-1, startSettings, handle)
	}

	// pass settings on the command line, e.g. --visu --steps --manual --delay 300
	def parseArgs(args: List[String], s: Settings): Settings = args match {
		case Nil => s
		case flag :: rest => flag match {
			case "--visu" | "-v" => parseArgs(rest, s.copy(visuMode = true))
			case "--steps"       => parseArgs(rest, s.copy(showSteps = true))
			case "--manual"      => parseArgs(rest, s.copy(autoSteps = false))
			case "--auto"        => parseArgs(rest, s.copy(autoSteps = true))
			case "--delay" => rest match {
				case value :: tl => parseArgs(tl, setDelay(value, s))
				case Nil         => s
			}
			case _ => parseArgs(rest, s)
		}
	}

	def command(line: String, s: Settings): Settings = line match {
		case ":visu" =>
			val next = s.copy(visuMode = !s.visuMode)
			println("lambda diagrams: " + settings.onOff(next.visuMode))
			next
		case ":steps" =>
			val next = s.copy(showSteps = !s.showSteps)
			println("beta reduction steps: " + settings.onOff(next.showSteps))
			next
		case ":auto" =>
			val next = s.copy(autoSteps = !s.autoSteps)
			println("auto steps: " + settings.onOff(next.autoSteps))
			next
		case ":delay" =>
			println("usage: :delay <ms>")
			s
		case d if d.startsWith(":delay ") =>
			setDelay(d.stripPrefix(":delay ").trim, s)
		case ":settings" =>
			println(settings.all(s))
			s
		case _ =>
			println("unknown command, try :settings")
			s
	}

	def setDelay(value: String, s: Settings): Settings = parseInt(value) match {
		case Some(ms) if ms >= 0 =>
			println("step delay: " + ms + " ms")
			s.copy(stepDelay = ms)
		case Some(_) =>
			println("delay must be >= 0")
			s
		case None =>
			println("usage: :delay <ms>")
			s
	}

	// read a decimal integer without throwing
	def parseInt(value: String): Option[Int] = {
		def digits(cs: List[Char], acc: Int): Option[Int] = cs match {
			case Nil => Some(acc)
			case c :: rest if c.isDigit => digits(rest, acc * 10 + (c - '0'))
			case _ => None
		}
		value.trim.toList match {
			case Nil         => None
			case '-' :: rest => digits(rest, 0).map(-_)
			case cs          => digits(cs, 0)
		}
	}

	def solve(input: String, s: Settings, h: console.Handle): Unit = {
		val result =
			try calculation.lambdaConverter(input)
			catch {
				case _: IllegalArgumentException       => "invalid equation"
				case _: UnsupportedOperationException => "invalid equation"
			}

		if (s.showSteps) {
			val steps =
				try betaReduction.betaReductionSteps(result)
				catch { case _: Throwable => List(result) }
			showSteps(steps, s, h)
		} else if (s.visuMode) {
			println("lambda term")
			println(johnTrompVisu.visualize(result))
			println("normal form")
			println(johnTrompVisu.visualize(betaReduction.betaReductionSolve(result)))
		} else {
			println("= " + result)
			val betaNormal = betaReduction.betaReductionSolve(result)
			println("= " + betaNormal)
			println("= " + calculation.lambdaToNaturalNumber(betaNormal))
		}
	}

	// walks the term through its beta reductions
	def showSteps(steps: List[String], s: Settings, h: console.Handle): Unit = {
		val last = steps.size - 1

		def go(rest: List[String], i: Int): Unit = rest match {
			case Nil => ()
			case term :: tl =>
				val name = if (i == last) "step " + i + " (normal form)" else "step " + i
				println(name + " / " + last)
				if (s.visuMode) println(johnTrompVisu.visualize(term))
				else println("  " + term)
				if (i < last) advance(s, h)
				go(tl, i + 1)
		}
		go(steps, 0)
	}

	// wait a bit in auto mode, otherwise wait for the user to press enter
	def advance(s: Settings, h: console.Handle): Unit =
		if (s.autoSteps) Thread.sleep(s.stepDelay.toLong)
		else console.readLine(h, "(enter to continue) ")

	// runs code for a given amount of runs
	// if n < 0 it will loop indefinitely
	def loop(n: Int, s: Settings, h: console.Handle): Unit =
		if (n != 0) {
			val input = console.readLine(h, "> ")
			if (input == null || input.trim.isEmpty) {
				println("bye")
				sys.exit(0)
			} else if (input.trim.startsWith(":")) {
				loop(n - 1, command(input.trim, s), h)
			} else {
				solve(input, s, h)
				loop(n - 1, s, h)
			}
		}
}
