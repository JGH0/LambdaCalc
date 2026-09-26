object lambda {

	// Converts a natural number n into its Church encoding:
	def naturalNumberToLambda(n: Int): String = {
		if (n < 0) {
			return ""
		} else {
			def helper(k: Int): String = {
				if (k == 0) {
					return "x"
				}
				else {
					return "f(" + helper(k - 1) + ")"
				}
			}
			return "λf.λx." + helper(n)
		}
	}
	// converts true/false to lambda
	def boolToLambda(n: Boolean): String = {
		if (n) {
			return "λx.λy.x"
		}
		return "λx.λy.y"
	}

	// converts 2 curch numbers into a sum of bolth
	def additionToLambda(x: String, y: String): String = {
		 return "(λm.λn.λf.λx. m f (n f x))(" + x + ")(" + y + ")"
	}

	// converts 2 curch numbers into a product of bolth
	def multiplicationToLambda(x: String, y: String): String = {
		return "(λm.λn.λf. m (n f))(" + x + ")(" + y + ")"
	}

	// converts 2 curch numbers into a power
	def powerToLambda(x: String, y: String): String = {
		return "(λm.λn. n m)(" + x + ")(" + y + ")"
	}

	// Converts a conventional equation to lambda-calculus term
	// TODO
	def lambdaConverter(term: String): String = {
		//naturalNumberToLambda(term.trim.toInt)
		return powerToLambda(naturalNumberToLambda(2),naturalNumberToLambda(3))
	}

	// Solves one step of lambda-calculus term given as a string.
	// TODO
	def lambdaSolver(term: String): String = {
		return term
	}
}
