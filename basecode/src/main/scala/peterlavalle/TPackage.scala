package peterlavalle

import scala.collection.convert.{WrapAsJava, WrapAsScala}
import scala.reflect.ClassTag

trait TPackage
	extends TContextDependantMapping
		with TXCommandLine
		with TXDate
		with TXFile
		with TXInputStream
		with TXIterable
		with TXMap
		with TXObject
		with TXOutputStream
		with TXString
		with TXThrowable
		with TXWriter
		with WrapAsScala with WrapAsJava {

	implicit class WrapClassTag[T](thisTag: ClassTag[T]) {
		def isAssignableTo[V](implicit themTag: ClassTag[V]): Boolean =
			themTag.runtimeClass.isAssignableFrom(thisTag.runtimeClass)
	}

	implicit def requyre[E <: Exception](condition: Boolean, messsage: => String)(implicit tag: ClassTag[E]): Unit = {
		if (!condition)
			throw tag.runtimeClass.getConstructor(classOf[String]).newInstance(messsage).asInstanceOf[E]
	}

	def ??? : Nothing = {
		val base = new NotImplementedError
		base.setStackTrace(base.getStackTrace.tail)

		while (base.getStackTrace.head.toString.trim.matches(".*\\$qmark\\$qmark\\$qmark\\$?\\([^\\)]+\\)$"))
			base.setStackTrace(base.getStackTrace.tail)

		val notImplementedError = new NotImplementedError(s"${base.getMessage} @ ${base.getStackTrace.head.toString.trim}")
		notImplementedError.setStackTrace(base.getStackTrace)
		throw notImplementedError
	}

}
