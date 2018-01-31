package peterlavalle

import junit.framework.TestCase
import org.junit.Assert.{assertEquals, fail}

class ATestCase extends TestCase {

	def sAssertEqual[T](expected: => T, actual: T): T =
		sAssertEqual(null, expected, actual)

	def sAssertEqual[T](message: => String, expected: => T, actual: T): T = {
		val e: T = expected

		if (e != actual) {
			def toS(d: Int, v: Stream[Char]): Stream[Char] =
				v match {
					case Stream.Empty => Stream.Empty
					case '(' #:: tail =>
						Stream('(', '\n') ++ (("\t" * (d + 1)) ++ toS(d + 1, tail))
					case ')' #:: tail =>
						Stream('\n') ++ (("\t" * (d - 1)) ++ Stream(')') ++ toS(d - 1, tail))
					case ',' #:: tail =>
						val next: Stream[Char] =
							if (' ' == tail.head)
								tail.tail
							else
								tail

						Stream(',', '\n') ++ (("\t" * (d)) ++ toS(d, next))
					case head #:: tail =>
						head #:: toS(d, tail)
				}

			assertEquals(
				message,
				new String(toS(0, e.toString.toStream).toArray),
				new String(toS(0, actual.toString.toStream).toArray)
			)

			fail("the values were not equal, but the strings are ...")
		}

		actual
	}
}
