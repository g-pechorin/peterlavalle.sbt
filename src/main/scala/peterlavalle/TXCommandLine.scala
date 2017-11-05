package peterlavalle

import java.io.{File, InputStream}

import org.codehaus.plexus.util.cli.{CommandLineUtils, Commandline, StreamConsumer}

trait TXCommandLine {

	implicit class WrappedCommandLine(commandLine: Commandline) {

		def invoke(out: String, err: String): Int =
			invoke(
				line => println(s"$out$line"),
				line => println(s"$err$line")
			)

		def invoke(out: String => Unit, err: String => Unit): Int =
			invoke(
				new StreamConsumer {
					override def consumeLine(line: String): Unit = out(line)
				},
				new StreamConsumer {
					override def consumeLine(line: String): Unit = err(line)
				}
			)

		def invoke(out: StreamConsumer, err: StreamConsumer): Int =
			CommandLineUtils.executeCommandLine(commandLine, out, err)

		def newArg(value: Any): Commandline = {
			value match {
				case f: File =>
					commandLine.createArg().setFile(f)
				case s: String =>
					commandLine.createArg().setValue(s.toString)
				case i: Int =>
					commandLine.createArg().setValue(i.toString)
			}
			commandLine
		}

		@deprecated("use the old shell - don't need/use stdin since it made quick-testing in V$ a pain")
		def pipe[R](input: InputStream, err: String => Unit = System.err.println)(out: Any => Any): R =
			CommandLineUtils.executeCommandLine(
				commandLine,
				input,
				new StreamConsumer {
					override def consumeLine(line: String): Unit = out(line)
				},
				new StreamConsumer {
					override def consumeLine(line: String): Unit = err(line)
				}
			) match {
				case r: Int =>
					out(r).asInstanceOf[R]
			}
	}

}
