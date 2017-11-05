name := "peterlavalle"

organization := "com.peterlavalle"

version := "2017.10.18"

scalaVersion := "2.11.11"

crossScalaVersions := Seq(
	"2.12.3",
	scalaVersion.value
).distinct

publishTo := Some(Resolver.file("file", new File("target/m2-repo")))

// https://mvnrepository.com/artifact/org.codehaus.plexus/plexus-utils
libraryDependencies += "org.codehaus.plexus" % "plexus-utils" % "3.1.0"

/*
git clone git@github.com:g-pechorin/m2-repo.git
git fetch
git checkout gh-pages
git pull
git add .
git commit -m "???"
 */
