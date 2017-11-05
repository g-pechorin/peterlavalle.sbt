package peterlavalle

trait TXString {

	implicit class WrappedString(value: String) {

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
	}

}
