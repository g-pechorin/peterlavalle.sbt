package peterlavalle

import java.util

class LazyCache[K, V](spawn: K => V) extends (K => V) {

	private val cache: util.HashMap[K, V] = new java.util.HashMap[K, V]()

	def apply(key: K): V =
		cache.synchronized {
			if (!cache.containsKey(key))
				cache.put(key, spawn(key))
			cache.get(key)
		}

	def ?(key: K): Boolean =
		cache.synchronized {
			cache.containsKey(key)
		}
}

object LazyCache {
	def apply[K, V](spawn: K => V): LazyCache[K, V] = new LazyCache[K, V](spawn)

	def kinder[V](spawn: Class[_] => V) =
		new LazyCache[Class[_], V](spawn)
}
