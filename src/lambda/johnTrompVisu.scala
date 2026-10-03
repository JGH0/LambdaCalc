/**
 * johnTrompVisu.scala
 *
 * Draws lambda terms as ASCII art in the style of John Tromp's Lambda Diagrams
 * (https://tromp.github.io/cl/diagrams.html):
 *   - abstractions are horizontal lines
 *   - variables are vertical lines hanging down from their binder
 *   - applications are horizontal bars joining the leftmost variables
 *
 * The layout is first computed on a small character grid (every variable
 * occurrence gets a 3 wide / 2 tall cell, cells are joined with a 1 wide gap),
 * then each filled cell is turned into a box drawing character based on its
 * neighbours.
 */
object johnTrompVisu {

	// ---------- de Bruijn AST ----------
	private sealed trait DTerm
	private final case class DVar(index: Int)                  extends DTerm
	private final case class DLam(body: DTerm)                 extends DTerm
	private final case class DApp(fn: DTerm, arg: DTerm)       extends DTerm

	// keep the drawn diagrams sane
	private val MaxWidth  = 400
	private val MaxHeight = 200

	// a grid is an immutable vector of rows, every row an immutable vector of chars
	private type Grid = Vector[Vector[Char]]

	// ---------- Public API ----------
	/** Draw a term given in the same syntax betaReduction parses. */
	def visualize(term: String): String = {
		try render(betaReduction.parseTerm(term))
		catch { case _: Throwable => "(could not draw term)" }
	}

	/** Draw an already parsed term. */
	def render(term: betaReduction.Term): String = {
		val cells = layout(toDeBruijn(term), 0)
		val w     = width(cells)
		val h     = height(cells)
		if (w > MaxWidth || h > MaxHeight) s"(diagram too large to draw: ${w}x$h)"
		else draw(cells)
	}

	// ---------- named -> de Bruijn ----------
	private def toDeBruijn(term: betaReduction.Term): DTerm = {
		def go(t: betaReduction.Term, env: List[String]): DTerm = t match {
			case betaReduction.Var(name) =>
				val i = env.indexOf(name)
				DVar(if (i < 0) -1 else i) // -1 marks a free variable
			case betaReduction.Lam(p, body) => DLam(go(body, p :: env))
			case betaReduction.App(fn, arg) => DApp(go(fn, env), go(arg, env))
		}
		go(term, Nil)
	}

	/** (abstractions above the variable, de Bruijn index) of every variable, left to right. */
	private def connections(t: DTerm, depth: Int): List[(Int, Int)] = t match {
		case DApp(fn, arg) => connections(fn, depth) ++ connections(arg, depth)
		case DLam(body)    => connections(body, depth + 1)
		case DVar(index)   => List((depth, index))
	}

	// ---------- layout ----------
	private def layout(t: DTerm, depth: Int): Grid = t match {
		case DVar(_) =>
			Vector(Vector(' ', '.', ' '), Vector(' ', '.', ' '))

		case DApp(fn, arg) =>
			val left  = layout(fn, depth)
			val right = layout(arg, depth)
			val wl = width(left);  val hl = height(left)
			val wr = width(right); val hr = height(right)
			val w = wl + wr + 1
			val h = math.max(hl, hr) + 2

			val joined  = overlay(overlay(blank(w, h), left, 0, 0), right, 0, wl + 1)
			// drop the leftmost line of both sides down to the bar
			val dropped = fillColumn(fillColumn(joined, 1, hl - 1, h - 1, '.'), wl + 2, hr - 1, h - 1, '.')
			// the application bar
			val barred  = fillRow(dropped, h - 2, 1, w - wr + 2, '.')
			put(barred, h - 1, 1, '.')

		case DLam(body) =>
			val d     = depth + 1
			val vars  = connections(body, d)
			val inner = layout(body, d)
			val w = width(inner)
			val h = height(inner) + 2

			val res       = fillRow(blank(w, h), 0, 0, w, '.') // the binder
			val withInner = overlay(res, inner, 2, 0)

			// connect the binder to every variable that reaches up to it
			vars.zipWithIndex.foldLeft(withInner) { case (acc, ((varDepth, index), i)) =>
				val pos = 1 + i * 4
				if (varDepth - index <= d && pos < w) put(acc, 1, pos, '.')
				else acc
			}
	}

	// ---------- grid helpers ----------
	private def blank(w: Int, h: Int): Grid = Vector.fill(h)(Vector.fill(w)(' '))

	private def width(cells: Grid): Int =
		if (cells.isEmpty) 0 else cells.map(_.length).max

	private def height(cells: Grid): Int = cells.length

	// immutable single cell update, ignored when it falls outside the grid
	private def put(cells: Grid, r: Int, c: Int, ch: Char): Grid =
		if (r < 0 || r >= cells.length || c < 0 || c >= cells(r).length) cells
		else cells.updated(r, cells(r).updated(c, ch))

	// set ch on row r for every column in [from, until)
	private def fillRow(cells: Grid, r: Int, from: Int, until: Int, ch: Char): Grid =
		if (from >= until) cells
		else fillRow(put(cells, r, from, ch), r, from + 1, until, ch)

	// set ch on column c for every row in [from, until)
	private def fillColumn(cells: Grid, c: Int, from: Int, until: Int, ch: Char): Grid =
		if (from >= until) cells
		else fillColumn(put(cells, from, c, ch), c, from + 1, until, ch)

	// paste src into dst with its top-left corner at (row, col)
	private def overlay(dst: Grid, src: Grid, row: Int, col: Int): Grid =
		src.zipWithIndex.foldLeft(dst) { case (acc, (srcRow, r)) =>
			srcRow.zipWithIndex.foldLeft(acc) { case (a, (ch, c)) =>
				if (ch == ' ') a else put(a, row + r, col + c, ch)
			}
		}

	// ---------- rendering ----------
	private def draw(cells: Grid): String = {
		val h = height(cells)
		val w = width(cells)

		def on(r: Int, c: Int): Boolean =
			r >= 0 && r < h && c >= 0 && c < cells(r).length && cells(r)(c) != ' '

		def charAt(r: Int, c: Int): Char =
			if (!on(r, c)) ' '
			else {
				val u  = on(r - 1, c)
				val dn = on(r + 1, c)
				val l  = on(r, c - 1)
				val rt = on(r, c + 1)

				if ((u || dn) && (l || rt)) (u, dn, l, rt) match {
					case (true,  true,  true,  true ) => '┼'
					case (true,  true,  true,  false) => '┤'
					case (true,  true,  false, true ) => '├'
					case (true,  false, true,  true ) => '┴'
					case (false, true,  true,  true ) => '┬'
					case (true,  false, true,  false) => '┘'
					case (true,  false, false, true ) => '└'
					case (false, true,  true,  false) => '┐'
					case _                            => '┌'
				}
				else if (u || dn) '│'
				else if (l || rt) '─'
				else              '·'
			}

		val lines = (0 until h).map { r =>
			(0 until w).map(c => charAt(r, c)).mkString.replaceAll("\\s+$", "")
		}
		lines.map(_ + "\n").mkString
	}
}
