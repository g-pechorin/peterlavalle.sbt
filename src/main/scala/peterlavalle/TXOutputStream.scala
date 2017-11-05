package peterlavalle

import java.io.OutputStream

import scala.collection.immutable.Stream.Empty

trait TXOutputStream {

	implicit class WrappedOutputStream[O <: OutputStream](outputStream: O) {

		def append(data: Stream[Byte]): O =
			append(data, 64 /* take 64 byte blocks */)

		def append(data: Stream[Byte], size: Int): O =
			data.splitAt(size) match {
				case (Empty, _) =>
					require(data.isEmpty)
					outputStream

				case (left, tail) =>
					require(left.nonEmpty)

					outputStream.write(left.toArray)

					append(tail, (size * 1.14).toInt)
			}
	}

}
