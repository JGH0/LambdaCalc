object betaReduction {

	// ---------- AST ----------
	sealed trait Term
	final case class Var(name: String)			  extends Term
	final case class Lam(param: String, body: Term) extends Term
	final case class App(fn: Term, arg: Term)	   extends Term

	// ---------- Public API ----------
	/** One leftmost-outermost (normal-order) β-step.
	 *  If the term is already in β-normal form, it is returned unchanged. */
	def betaReductionStep(term: String): String =
		reduceOnce(parse(term)) match {
			case Some(t) => render(t)
			case None    => term
		}

	/** Reduce to β-normal form, with a hard step cap to avoid infinite loops
	 *  on non-terminating terms (e.g. Ω = (λx.xx)(λx.xx)). */
	def betaReductionSolve(term: String): String = {
		val maxSteps = 10000

		@scala.annotation.tailrec
		def loop(t: Term, steps: Int): Term =
			if (steps <= 0) t
			else reduceOnce(t) match {
				case Some(t2) => loop(t2, steps - 1)
				case None     => t
			}

		render(loop(parse(term), maxSteps))
	}

	// ---------- Parsing ----------

	private def parse(s: String): Term = {
		val (t, rest) = parseExpr(tokenize(s))
		if (rest.nonEmpty)
			throw new IllegalArgumentException(s"Trailing tokens: $rest")
		t
	}

	private def tokenize(s: String): List[String] = {
		def isSpecial(c: Char): Boolean =
			c.isWhitespace || c == '.' || c == '(' || c == ')' || c == 'λ' || c == '\\'

		@scala.annotation.tailrec
		def loop(rest: String, acc: List[String]): List[String] =
			if (rest.isEmpty) acc.reverse
			else rest.head match {
				case c if c.isWhitespace => loop(rest.tail, acc)
				case 'λ' | '\\'          => loop(rest.tail, "λ" :: acc)
				case '.'                 => loop(rest.tail, "." :: acc)
				case '('                 => loop(rest.tail, "(" :: acc)
				case ')'                 => loop(rest.tail, ")" :: acc)
				case _ =>
					val (name, tail) = rest.span(c => !isSpecial(c))
					loop(tail, name :: acc)
			}
		loop(s, Nil)
	}

	// λ binds loosest; application is left-associative; atoms are vars / parens.
	private def parseExpr(tokens: List[String]): (Term, List[String]) = tokens match {
		case "λ" :: name :: "." :: tl =>
			val (body, rest) = parseExpr(tl) // λ extends as far right as possible
			(Lam(name, body), rest)
		case _ =>
			parseApp(tokens)
	}

	private def parseApp(tokens: List[String]): (Term, List[String]) = {
		val (first, rest) = parseAtom(tokens)

		@scala.annotation.tailrec
		def loop(acc: Term, rest: List[String]): (Term, List[String]) = rest match {
			case t :: _ if startsAtom(t) =>
				val (arg, r2) = parseAtom(rest)
				loop(App(acc, arg), r2)
			case _ => (acc, rest)
		}
		loop(first, rest)
	}

	private def startsAtom(t: String): Boolean =
		t != ")" && t != "."

	private def parseAtom(tokens: List[String]): (Term, List[String]) = tokens match {
		case "(" :: tl =>
			val (e, rest) = parseExpr(tl)
			rest match {
				case ")" :: r => (e, r)
				case _	=> throw new IllegalArgumentException("Missing )")
			}
		case "λ" :: name :: "." :: tl =>
			val (body, rest) = parseExpr(tl)
			(Lam(name, body), rest)
		case name :: tl if startsAtom(name) =>
			(Var(name), tl)
		case t :: _ =>
			throw new IllegalArgumentException(s"Unexpected token: $t")
		case Nil =>
			throw new IllegalArgumentException("Unexpected end of input")
	}

	// ---------- Reduction ----------
	// Leftmost-outermost redex only.
	private def reduceOnce(t: Term): Option[Term] = t match {
		case App(Lam(x, body), arg) =>
			Some(substitute(body, x, arg))
		case App(f, a) =>
			reduceOnce(f) match {
				case Some(f2) => Some(App(f2, a))
				case None	 => reduceOnce(a).map(App(f, _))
			}
		case Lam(x, b) =>
			reduceOnce(b).map(Lam(x, _))
		case Var(_) =>
			None
	}

	// ---------- Capture-avoiding substitution ----------
	private def freeVars(t: Term): Set[String] = t match {
		case Var(x)	=> Set(x)
		case Lam(x, b) => freeVars(b) - x
		case App(f, a) => freeVars(f) ++ freeVars(a)
	}

	private def fresh(base: String, taken: Set[String]): String = {
		@scala.annotation.tailrec
		def loop(i: Int): String = {
			val c = base + i
			if (taken.contains(c)) loop(i + 1) else c
		}
		loop(1)
	}

	/** t[x := s], renaming bound vars of t when needed to avoid capture. */
	private def substitute(t: Term, x: String, s: Term): Term = t match {
		case Var(y)	=> if (y == x) s else Var(y)
		case App(f, a) => App(substitute(f, x, s), substitute(a, x, s))
		case Lam(y, b) =>
			if (y == x) Lam(y, b) // shadowed — stop
			else if (freeVars(s).contains(y)) { // would capture -> rename
				val z = fresh(y, freeVars(b) ++ freeVars(s) + x)
				Lam(z, substitute(substitute(b, y, Var(z)), x, s))
			} else Lam(y, substitute(b, x, s))
	}

	// ---------- Rendering ----------
	// Minimal parens: only when needed by precedence.
	private def render(t: Term): String = t match {
		case Var(x)	=> x
		case Lam(x, b) => "λ" + x + "." + render(b)
		case App(f, a) => renderFn(f) + " " + renderArg(a)
	}
	private def renderFn(t: Term): String = t match {
		case Lam(_, _) => "(" + render(t) + ")"
		case _ => render(t)
	}
	private def renderArg(t: Term): String = t match {
		case Var(_) => render(t)
		case _ => "(" + render(t) + ")"
	}
}
