import scala.io.StdIn

object main {

	def main(args: Array[String]): Unit = {
		println("Calculator started, enter an equation to solve (empty line to quit):")
		loop(-1) {
			val input = StdIn.readLine("> ")
			if (input == null || input.trim.isEmpty) {
				println("bye")
				sys.exit(0)
			}
			val result =
				try calculation.lambdaConverter(input)
				catch {
					case _: IllegalArgumentException       => "invalid equation"
					case _: UnsupportedOperationException => "invalid equation"
				}
			println("= " + result)
		}
	}

	// runs code for a given amount of runs
	// if n < 0 it will loop indefinitely
	def loop(n: Int)(body: => Unit): Unit = {
		if (n != 0) {
			body
			loop(n - 1)(body)
		}
	}
}
