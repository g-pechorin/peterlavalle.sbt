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

		def clusterBy[K](lambda: T => K): Stream[(K, Iterable[T])] = {

			def recur(tuples: Stream[(K, T)]): Stream[(K, Iterable[T])] =
				tuples match {
					case Empty => Empty

					case (key: K, value: T) #:: tail =>
						def isKey(kv: (K, T)): Boolean = kv._1 == key

						(key,
							value :: tail.takeWhile(isKey).toList.map((_: (K, T))._2)
						) #:: recur(tuples.dropWhile(isKey))
				}

			recur(
				value.toStream.map {
					i: T =>
						(lambda(i), i)
				}
			)
		}

		@deprecated(
			"unclear to me what this is trying to do that `expand()` doesn't do",
			"2018-01-13"
		)
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

		/**
			* expands each element in an iterable, and, recurs on the expanded elements
			*
			* @param expansion to logic to expand an element
			* @return a yuge stream of stuff
			*/
		def expand(expansion: T => Iterable[T]): Stream[T] =
			if (value.isEmpty)
				Stream()
			else
				value.head #:: (expansion(value.head).toStream ++ value.tail).expand(expansion)

		def filterTo[E](implicit classTag: ClassTag[E]): Iterable[E] =
			value
				.filter(classTag.runtimeClass.isInstance)
				.toList
				.map((_: T).asInstanceOf[E])

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
