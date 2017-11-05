package peterlavalle

import java.io.{File, FileReader}

import org.codehaus.plexus.util.cli.Commandline

trait TXFile {

	implicit class WrappedFile(file: File) {

		require(null != file)

		private val value: File =
			if (file != file.getAbsoluteFile)
				file.getAbsoluteFile
			else
				file

		assume(value.getAbsolutePath == file.getAbsolutePath)

		def Shell(command: File): Commandline =
			Shell(command.AbsolutePath)

		def Shell(command: String): Commandline = {

			require(null != command)

			val commandLine = new Commandline()

			commandLine.setWorkingDirectory(value.AbsolutePath)

			commandLine.setExecutable(command)

			commandLine
		}


		/**
			* relative path from `this` to `them`
			*/
		def PathTo(them: File): String = {
			def recu(m: List[String], t: List[String]): String =
				(m, t) match {
					case (Nil, some) =>
						some.reduce(_ + "/" + _)

					case ((mh :: mt), (th :: tt)) =>
						require(mh == th)
						recu(mt, tt)
				}

			recu(
				value.ParentFile.AbsolutePath.split("/").toList,
				them.AbsolutePath.split("/").toList
			)
		}

		def AbsolutePath: String =
			value.getAbsolutePath.replace('\\', '/')

		def ParentFile: File = value.getAbsoluteFile.getParentFile

		def wipedDir: File = {
			if (file.exists()) {
				requyre[Exception](
					file.isDirectory,
					s"Not a directory ${file.AbsolutePath}"
				)

				requyre[Exception](
					file.unlink,
					s"Failed to unlink ${file.AbsolutePath}"
				)
			}

			requyre[Exception](
				file.mkdirs(),
				s"Failed to create folder ${file.AbsolutePath}"
			)

			file
		}

		def unlink: Boolean =
			(!value.exists()) || {
				require(value.isDirectory)

				(value **).foreach {
					path =>
						require((value / path).delete())
				}
				value.delete()
			}

		def wipedFile: File = {
			if (file.exists()) {
				requyre[Exception](
					file.isFile,
					s"Not a directory ${file.AbsolutePath}"
				)
				requyre[Exception](
					file.delete(),
					s"Failed to delete ${file.AbsolutePath}"
				)
			}
			file.EnsureParent
		}

		def EnsureParent: File = {
			ParentFile.EnsureExists
			value.getAbsoluteFile
		}

		def EnsureExists: File = {
			require(
				value.exists() || value.mkdirs()
			)
			value
		}

		def **(pattern: String): Stream[String] = ** filter (_ matches pattern)

		def ** : Stream[String] = {

			def recu(todo: List[String]): Stream[String] =
				todo match {
					case Nil => Stream.Empty

					case path :: tail =>
						val file = value / path
						val list = file.list()

						if (file.isDirectory && null != list)
							recu(tail ++ list.map(path + '/' + _))
						else
							path #:: recu(tail)
				}

			require(null != value)
			recu(value.list() match {
				case null => Nil
				case list => list.toList
			})
		}

		def /(path: String): File = {

			def recu(file: File, todo: List[String]): File =
				todo match {
					case ".." :: tail =>
						recu(file.getParentFile, tail)
					case next :: tail =>
						recu(
							new File(file, next),
							tail
						)
					case Nil =>
						file
				}

			recu(
				value,
				path.split("/").toList
			)
		}

		def isNewer(other: File): Boolean =
			!(other.exists() && value.lastModified() < other.lastModified())

		def isOlder(stamp: Long): Boolean =
			value.lastModified() <= stamp

		import scala.language.postfixOps

		def makeFolder: File = {
			require(value.exists() || value.mkdirs())
			value
		}

		def makeParentFolder: File = {
			require(value.getParentFile.exists() || value.getParentFile.mkdirs())
			value
		}

		def makeString: String = {
			val fileReader = new FileReader(value)

			def recu: String = {
				val buffer = Array.ofDim[Char](512)
				fileReader.read(buffer) match {
					case -1 =>
						fileReader.close()
						""
					case read =>
						require(0 < read)
						new String(buffer.take(read)) + recu
				}

			}

			recu
		}
	}

}
