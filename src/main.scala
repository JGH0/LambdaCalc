import scala.io.StdIn

object main {

	def main(args: Array[String]): Unit = {
		// draw john Tromp lambda diagrams instead of the plain solution
		// (or start with --visu / -v, or type :visu while running to toggle it)
		val startVisu = args.exists(a => a == "--visu" || a == "-v")

		println("Calculator started, enter an equation to solve (empty line to quit):")
		println("type :visu to toggle the lambda diagram mode")
		loop(-1, startVisu)
	}

	// runs code for a given amount of runs
	// if n < 0 it will loop indefinitely
	def loop(n: Int, visuMode: Boolean): Unit =
		if (n != 0) {
			val input = StdIn.readLine("> ")
			if (input == null || input.trim.isEmpty) {
				println("bye")
				sys.exit(0)
			} else if (input.trim == ":visu") {
				val next = !visuMode
				println("lambda diagrams: " + (if (next) "on" else "off"))
				loop(n - 1, next)
			} else {
				solve(input, visuMode)
				loop(n - 1, visuMode)
			}
		}

	def solve(input: String, visuMode: Boolean): Unit = {
		val result =
			try calculation.lambdaConverter(input)
			catch {
				case _: IllegalArgumentException       => "invalid equation"
				case _: UnsupportedOperationException => "invalid equation"
			}
		if (visuMode) {
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
}
