package peterlavalle

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util
import java.util.zip.{ZipEntry, ZipFile}

import scala.sys.process._

class DumpCache(root: File) {
	def apply(url: String): File = {
		val path = url.replaceAll("[_[^\\w]]+", "_")
		val dir = root / path

		if (!dir.exists()) {

			val zip = File.createTempFile(path, ".zip")

			DumpCache.download(
				url,
				zip
			)

			DumpCache.extract(
				zip,
				dir
			)

			require(zip.delete())
		}

		require(dir.exists())
		dir
	}
}

object DumpCache {
	def download(url: String, zip: File): Unit = {
		new URL(url) #> zip.makeParentFolder !!
	}

	def extract(zip: File, dir: File): Unit = {
		val zipFile = new ZipFile(zip)

		def recu(entries: util.Enumeration[_]): Unit =
			if (!entries.hasMoreElements) {
				zipFile.close()
			} else {
				entries.nextElement() match {
					case next: ZipEntry =>
						if (!next.isDirectory)
							Files.copy(
								zipFile.getInputStream(next),
								(dir / next.getName).makeParentFolder.toPath
							)
						recu(entries)
				}
			}

		zipFile.entries() match {
			case entries =>
				recu(
					entries
				)
		}
	}
}
