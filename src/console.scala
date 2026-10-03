import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions

/**
 * console.scala
 *
 * Thin wrapper around the terminal input. In a real terminal we use jline so the
 * arrow keys work like in a shell (command history + editing). If the input is
 * piped (no tty) we just fall back to reading plain lines.
 *
 * The reader is handed back as an immutable Handle so no mutable state is kept
 * around; the caller threads the handle through instead.
 */
object console {

	private val historyFile = Paths.get(System.getProperty("user.home"), ".lambdaCalc_history")

	/** An immutable handle to the input source; `None` means "no line editing". */
	final case class Handle(reader: Option[LineReader])

	// keep the history file private so jline does not warn about it
	private def touchHistory(): Unit =
		try {
			val perms = PosixFilePermissions.fromString("rw-------")
			if (!Files.exists(historyFile))
				Files.createFile(historyFile, PosixFilePermissions.asFileAttribute(perms))
			else
				Files.setPosixFilePermissions(historyFile, perms)
		} catch { case _: Throwable => () }

	def init(): Handle =
		if (System.console() == null) Handle(None) // piped, no line editing possible
		else {
			touchHistory()
			try {
				val terminal = TerminalBuilder.builder().system(true).build()
				val r = LineReaderBuilder.builder()
					.terminal(terminal)
					.variable(LineReader.HISTORY_FILE, historyFile)
					.build()
				r.getHistory // let jline pick up the history file
				Handle(Some(r))
			} catch {
				case _: Throwable => Handle(None)
			}
		}

	/** Read one line (with history when we have a terminal). null on EOF / Ctrl-C. */
	def readLine(handle: Handle, prompt: String): String = handle.reader match {
		case None    => scala.io.StdIn.readLine(prompt)
		case Some(r) =>
			try r.readLine(prompt)
			catch {
				case _: EndOfFileException     => null
				case _: UserInterruptException => null
			}
	}
}
