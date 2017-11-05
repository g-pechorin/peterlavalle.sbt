package peterlavalle

import java.io.Writer

trait TXWriter {

	implicit class WrappedWriter[W <: Writer](value: W) {

		def appund[T](monad: Option[T])(tostr: T => String): W =
			monad match {
				case None => value
				case Some(thing) =>
					value.appund(tostr(thing))
			}

		def appund[E](many: Iterable[E])(tostr: E => String): W =
			appund(many.iterator)(tostr)

		def appund[E](many: Iterator[E])(tostr: E => String): W =
			many.foldLeft(value)((w: W, e: E) => w.appund(tostr(e)))

		def appund[E](text: String): W =
			value.append(text).asInstanceOf[W]
	}

}
