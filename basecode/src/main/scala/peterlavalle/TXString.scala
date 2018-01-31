package peterlavalle

import java.io.{ByteArrayInputStream, InputStream}

trait TXString {

	implicit class WrappedString(value: String) {

		def toInputStream: InputStream =
			new ByteArrayInputStream(value.getBytes())

		def stripTrim: String = value.stripMargin.trim + '\n'

		def tailTrim: String = value.stripMargin.replaceAll("\\s*$", "\n")

		def halt: Nothing = {

			val base = new RuntimeException
			base.setStackTrace(base.getStackTrace.tail.tail)

			val runtimeException = new RuntimeException(s"$value @ ${base.getStackTrace.head.toString.trim}")
			runtimeException.setStackTrace(base.getStackTrace)
			throw runtimeException
		}

		def stripMarginTail: String =
			value
				.trim
				.stripMargin + "\n"

		def reIndent(indent: Int): String =
			value
				.replaceAll("\n|^", "$0" + ("\t" * indent))
				.replaceAll("[\t ]+$", "")

		def reReplace(pattern: String, replace: String): String = {
			val result: String = value.replaceAll(pattern, replace)

			if (result == value)
				value
			else
				result.reReplace(pattern, replace)
		}
	}

}
