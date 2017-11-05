package peterlavalle

import java.lang.{Iterable => JIterable}
import java.util.{Iterator => JIterator}

import scala.collection.immutable.Stream.Empty
import scala.language.implicitConversions
import scala.reflect.ClassTag

trait TXIterable {

	implicit def wrapJIterable[T](value: JIterable[T]): WrappedIterable[T] = {
		new WrappedIterable[T](new Iterable[T] {
			override def iterator: Iterator[T] =
				new Iterator[T] {
					val core: JIterator[T] = value.iterator()

					def hasNext: Boolean = core.hasNext

					def next(): T = core.next()
				}
		})
	}

	implicit class WrappedIterable[T](value: Iterable[T]) {

		def explode(what: T => Iterable[T]): Stream[T] =
			value match {
				case Stream() => Empty

				case head #:: tail =>
					val next: Stream[T] =
						what(head) match {
							case next: Stream[T] => next
							case next: Iterable[T] => next.toStream
						}

					// use cons to be tots sure the we'll get a lazy
					Stream.cons(
						head,
						next ++ tail
					)

				case _ =>
					value.toStream.explode(what)
			}

		def filterTo[E](implicit classTag: ClassTag[E]): List[E] =
			value
				.filter(i => classTag.runtimeClass.isInstance(i))
				.toList
				.map(_.asInstanceOf[E])

		def foldIn(fold: (T, T) => T): T =
			value.tail.foldLeft(value.head)(fold)

		def uniqueBy[K](f: (T) ⇒ K): Boolean =
			value
				.groupBy(f)
				.forall(1 == _._2.size)

		def toHashSet: Set[T] =
			value.toList.sortBy(_.hashCode()).toSet

		def mergeBy[V <: Comparable[V]](them: Iterable[T])(query: T => V): Stream[T] = {

			if (value.isEmpty)
				them.toStream
			else if (them.isEmpty)
				value.toStream
			else {
				val s: V = query(value.head)
				val t: V = query(them.head)
				s.compareTo(t) match {
					case i if i < 0 =>
						value.head #:: value.tail.mergeBy(them)(query)
					case 0 =>
						value.mergeBy(them.tail)(query)
					case i if 0 < i =>
						them.head #:: value.mergeBy(them.tail)(query)
				}
			}
		}

		def distinctBy[V](query: T => V): Stream[T] = {

			def recu(done: Set[V], todo: Stream[T]): Stream[T] =
				todo match {
					case Empty => Empty

					case head #:: tail =>

						val key = query(head)

						if (done(key))
							recu(done, tail)
						else
							head #:: recu(
								done + key,
								tail
							)
				}


			recu(Set(), value.toStream)
		}
	}

}
