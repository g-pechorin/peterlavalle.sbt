package peterlavalle

import java.io.OutputStream

import scala.collection.immutable.Stream.Empty

trait TXObject {

	implicit class WrapObject[L <: Object](self: L) {
		def rollLeft[N](iterable: Iterable[N])(operation: (L, N) => L): L =
			iterable.foldLeft(self)(operation)
	}

}
