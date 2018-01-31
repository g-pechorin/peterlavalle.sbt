package peterlavalle

import java.io.File

import org.codehaus.plexus.util.cli.StreamConsumer

class Mercurial private(val root: File) extends Mercurial.TPlumbing {

	def branch: String =
		log(0, "{branch}") match {
			case (0, branch, "") =>
				branch.trim
		}
	def version: String =
		log(0, "{node}") match {
			case (0, branch, "") =>
				branch.trim
		}
}

object Mercurial {
	def of(file: File): Mercurial =
		if ((file / ".hg").exists())
			new Mercurial(file)
		else
			Mercurial.of(file.ParentFile)

	sealed trait TPlumbing {
		val root: File

		def log(length: Int, template: String): (Int, String, String) = {
			val sO: StringBuilder = new StringBuilder()
			val sE: StringBuilder = new StringBuilder()

			object lOut extends StreamConsumer {
				override def consumeLine(o: String): Unit = sO.append(o).append('\n')
			}
			object lErr extends StreamConsumer {
				override def consumeLine(e: String): Unit = sE.append(e).append('\n')
			}

			root.Shell("hg")
				.newArgs(
					"log",
					if (0 == length)
						"-r."
					else
						s"-l$length",
					"--template", template
				)
				.invoke(lOut, lErr) match {
				case r: Int =>
					(r, sO.toString(), sE.toString())
			}
		}
	}
}
