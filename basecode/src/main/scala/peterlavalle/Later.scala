package peterlavalle

import scala.reflect.ClassTag

/**
	* This is (or should be) a sort of lazy value that has to be explicitly initialised
	*/
trait Later[T] {

	def ??[V](handle: Option[T] => V): V

	def wrap[O](wrapper: T => O)(implicit oTag: ClassTag[T], iTag: ClassTag[O]): Later[O] =
		new peterlavalle.Later.PassThrough[O, T](this, wrapper)

	/**
		* quick and dirty use of .map()
		*/
	def filter[I](query: I => Boolean)(implicit iTag: ClassTag[I]): Iterable[I] =
		map[I, (Boolean, I)](i => (query(i), i)).filter(_._1).map(_._2)

	/**
		* assumes that we're getting an instance of an iterable; this does a mapping
		*/
	def map[I, O](operation: I => O)(implicit iTag: ClassTag[I], oTag: ClassTag[O]): Iterable[O] =
		get match {
			case iterable: Iterable[I] =>
				iterable map operation
		}

	def get: T =
		this ? {
			(v: T) => v
		}

	def ?[V](handle: T => V): V =
		this ?? {
			case Some(value) =>
				handle(value)
			case None =>
				throw new Later.NotReadyException
		}
}

object Later {

	def Stub[T](message: String = "missing"): Later[T] = {
		new Later[T] {
			override def ??[V](handle: Option[T] => V): V = {
				sys.error(message)
			}
		}
	}

	class NotReadyException(message: String) extends Exception(message) {
		def this() = this("a later is not ready")
	}

	class PassThrough[O, I](real: Later[I], wrapper: I => O)(implicit oTag: ClassTag[O], iTag: ClassTag[I]) extends Later[O] {
		override def ??[V](handle: Option[O] => V) =
			real ?? {
				case Some(o: I) =>
					handle(Some(wrapper(o)))
			}
	}

	class SetOnce[T] {
		private var value: Option[T] = None

		def :=(value: T): Unit =
			this.value match {
				case None =>
					this.value = Some(value)
			}

		def later: Later[T] =
			new Later[T] {
				override def ??[V](handle: (Option[T]) => V): V =
					handle(value)
			}


		def withError(message: => String): SetOnce[T] =
			new SetOnce[T] {
				override def later: Later[T] =
					new Later[T] {
						override def ??[V](handle: (Option[T]) => V): V =
							try {
								handle(value)
							} catch {
								case _: Later.NotReadyException =>
									throw new NotReadyException(message)
							}
					}
			}
	}

}
