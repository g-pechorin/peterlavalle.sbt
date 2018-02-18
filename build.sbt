

lazy val commonSettings =
	Seq(
		organization := "com.peterlavalle",
		version := {
			//
			// generate a version string
			if (System.getProperty("release", "false").toBoolean) {
				import java.text.SimpleDateFormat
				import java.util.{Date, Locale, TimeZone}
				val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK)
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
				"v" + dateFormat.format(new Date())
			} else {
				import sys.process._
				"hg log -r. --template {branch}-SNAPSHOT".!!.trim
			}
		},
		scalaVersion := "2.12.3",
		publishTo := Some(Resolver.file("file", new File("target/m2-repo"))),
		libraryDependencies ++= Seq(
			"junit" % "junit" % "4.12" % Test
		)
	)


lazy val basecode =
	project
		.settings(
			name := "peterlavalle",
			commonSettings,

			libraryDependencies ++= Seq(
				"org.codehaus.plexus" % "plexus-utils" % "3.1.0"
			)
		)
lazy val merc =
	project
		.settings(
			name := "merc",
			commonSettings
		)
		.dependsOn(basecode)

lazy val junit =
	project
		.settings(
			name := "junit",
			commonSettings,
			libraryDependencies ++= Seq(
				"junit" % "junit" % "4.12",
				"org.easymock" % "easymock" % "3.5.1"
			)
		)
		.dependsOn(
			basecode
		)

lazy val antlr =
	project
		.settings(
			name := "antlr",
			commonSettings,

			// ... hmm ... seems to do a chicken-or-egg recurisve problem
			libraryDependencies ++= Seq(
				"org.antlr" % "antlr4-runtime" % "4.7",
				"org.antlr" % "antlr4" % "4.7"
			)
		)
		.dependsOn(
			basecode
		)

lazy val swung =
	project
		.settings(
			name := "peterlavalle-swung",
			commonSettings
		)
		.dependsOn(
			basecode
		)

lazy val sstate =
	(project in file("sstate"))
		.settings(
			name := "sstate",
			commonSettings
		)

lazy val root =
	(project in file("."))
		.aggregate(
			antlr,
			basecode,
			junit,
			merc,
			sstate,
			swung
		)
		.settings(
			name := "peterlavalle-root",
			commonSettings
		)



/*
	git clone git@github.com:g-pechorin/m2-repo.git
	git fetch
	git checkout gh-pages
	git pull
	git add .
	git commit -m "???"
*/
