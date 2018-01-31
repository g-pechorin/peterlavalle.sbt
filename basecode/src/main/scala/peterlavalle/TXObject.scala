package peterlavalle

import scala.reflect.ClassTag

trait TXObject {

	implicit class WrapObject[L <: Object](self: L) {
		def rollLeft[N](iterable: Iterable[N])(operation: (L, N) => L): L =
			iterable.foldLeft(self)(operation)

		def notNull[E <: Exception](message: => String)(implicit classTag: ClassTag[E]): L = {
			requyre[E](
				null != self,
				message
			)
			self
		}

		def ifNullOrMap[V](otherwise: V)(lambda: L => V): V =
			if (null != self)
				lambda(self)
			else
				otherwise


	}

}
