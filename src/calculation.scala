/**
 * Calculation.scala
 *
 * Translates conventional mathematical expressions into their Church-encoded
 * lambda-calculus representation.
 *
 * Pipeline:
 *   1. Strip all whitespace from the input term.
 *   2. Tokenize into numbers, operators, parentheses, and `$`-prefixed atoms.
 *   3. Parse with precedence climbing into an Expr AST
 *      (`^` > `*`, `/` > `+`, `-`, with `^` right-associative).
 *   4. Encode the AST into lambda terms via the `lambda` object
 *      (Church numerals, Church booleans, addition, multiplication, power).
 *
 * `$`-atoms:
 *   `$<sym>` marks the following token as a literal atom rather than a number.
 *   `$true` / `$false` map to Church booleans; `$+`, `$*`, etc. are emitted
 *   verbatim as symbols, so `$++$+` becomes `+` added to `+`.
 *
 */
object calculation {

	// ---------- AST ----------
	sealed trait Expr
	final case class Num(n: Int)                       extends Expr
	final case class Atom(sym: String)                 extends Expr
	final case class BinOp(op: Char, l: Expr, r: Expr) extends Expr

	// ---------- Public entry ----------
	def lambdaConverter(term: String): String = {
		val cleaned = term.replaceAll("\\s", "")
		val tokens  = tokenize(cleaned)
		if (tokens.isEmpty) ""
		else {
			val (expr, rest) = parseExpr(tokens, 0)
			require(rest.isEmpty, s"Trailing tokens: $rest")
			toLambda(expr)
		}
	}

	// ---------- Tokenizer  ----------
	private def tokenize(s: String): List[String] = {
		@scala.annotation.tailrec
		def loop(rest: String, acc: List[String]): List[String] =
			if (rest.isEmpty) acc.reverse
			else rest.head match {

				case '$' =>
					val after = rest.tail
					if (after.isEmpty)
						throw new IllegalArgumentException("Dangling '$'")
					else if ("+-*/^".contains(after.head))
						loop(after.tail, ("$" + after.head) :: acc)
					else if (after.head.isLetter) {
						val (name, tail) = after.span(_.isLetterOrDigit)
						loop(tail, ("$" + name) :: acc)
					} else
						throw new IllegalArgumentException(s"Bad char after '$$': ${after.head}")

				case c if c.isDigit =>
					val (num, tail) = rest.span(_.isDigit)
					loop(tail, num :: acc)

				case c if "+-*/^()".contains(c) =>
					loop(rest.tail, c.toString :: acc)

				case c =>
					throw new IllegalArgumentException(s"Unexpected char: $c")
			}

		loop(s, Nil)
	}

	// ---------- Parser (precedence climbing) ----------
	private val Prec	   = Map('+' -> 1, '-' -> 1, '*' -> 2, '/' -> 2, '^' -> 3)
	private val RightAssoc = Set('^')

	private def parseExpr(tokens: List[String], minPrec: Int): (Expr, List[String]) = {
		val (first, rest0) = parseAtom(tokens)

		def climb(left: Expr, rest: List[String]): (Expr, List[String]) = rest match {
			case op :: tl if op.length == 1 && Prec.contains(op.charAt(0)) =>
				val c	= op.charAt(0)
				val prec = Prec(c)
				if (prec < minPrec) (left, rest)
				else {
					val nextMin	 = if (RightAssoc(c)) prec else prec + 1
					val (right, r2) = parseExpr(tl, nextMin)
					climb(BinOp(c, left, right), r2)
				}
			case _ => (left, rest)
		}

		climb(first, rest0)
	}

	private def parseAtom(tokens: List[String]): (Expr, List[String]) = tokens match {
		case Nil => throw new IllegalArgumentException("Unexpected end of input")

		case "(" :: tl =>
			val (e, rest) = parseExpr(tl, 0)
			rest match {
				case ")" :: r => (e, r)
				case _		=> throw new IllegalArgumentException("Missing )")
			}

		case t :: tl if t.startsWith("$") =>
			(Atom(t.drop(1)), tl)

		case t :: tl if t.nonEmpty && t.forall(_.isDigit) =>
			(Num(t.toInt), tl)

		case t :: _ =>
			throw new IllegalArgumentException(s"Unexpected token: $t")
	}

	// ---------- Encoder ----------
	private def toLambda(e: Expr): String = e match {
		case Num(n)           => lambda.naturalNumberToLambda(n)
		case Atom("true")     => lambda.boolToLambda(true)
		case Atom("false")    => lambda.boolToLambda(false)
		case Atom(sym)        => sym
		case BinOp('+', l, r) => lambda.additionToLambda(toLambda(l), toLambda(r))
		case BinOp('*', l, r) => lambda.multiplicationToLambda(toLambda(l), toLambda(r))
		case BinOp('^', l, r) => lambda.powerToLambda(toLambda(l), toLambda(r))
		case BinOp(op, _, _)  => throw new UnsupportedOperationException(s"Unsupported op: $op")
	}

	//tries to decode chuch nubers to decimal numbers
	def lambdaToNaturalNumber(lambdaTerm: String): String = {
		val prefix = "λf.λx."

		// skip Whitespace that the endoder doesn't need
		def skipWs(s: String, i: Int): Int =
			if (i < s.length && s.charAt(i).isWhitespace) {
				return skipWs(s, i + 1)
			} else {
				return i
			}

		def decodeBody(s: String, start: Int): Option[(Int, Int)] = {
			val i = skipWs(s, start)
			if (i >= s.length) {
				return None
			}
			else s.charAt(i) match {
				case 'x' => Some((0, i + 1))
				case 'f' =>
					val j = skipWs(s, i + 1)
					if (j < s.length && s.charAt(j) == '('){
						decodeBody(s, j + 1).flatMap { case (n, end) =>
							val k = skipWs(s, end)
							if (k < s.length && s.charAt(k) == ')'){
								Some((n + 1, k + 1))
							} else {
								None
							}
						}
					} else if (j < s.length && s.charAt(j) == 'x') {
						Some((1, j + 1))
					} else {
						return None
					}
				case _ => return None
			}
		}

		def loop(i: Int, acc: List[String]): String =
			if (i >= lambdaTerm.length) {
				acc.reverse.mkString
			} else if (lambdaTerm.startsWith(prefix, i)) {
				decodeBody(lambdaTerm, i + prefix.length) match {
					case Some((n, end)) => loop(end, n.toString :: acc)
					case None           => loop(i + 1, lambdaTerm.charAt(i).toString :: acc)
				}
			} else {
				loop(i + 1, lambdaTerm.charAt(i).toString :: acc)
			}
		loop(0, Nil)
	}
}
