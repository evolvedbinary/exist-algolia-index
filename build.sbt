
name := "exist-algolia-index"

version := "1.0"

scalaVersion := "2.12.0"

libraryDependencies ++= {

  val scalazV = "7.2.7"
  val fs2V = "0.9.2"
  val existV = "201701082031-SNAPSHOT"
  val algoliaV = "2.7.0"
  val akkaV = "2.4.14"
  val jacksonV = "2.7.4"

  Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
    "org.scalaz" %% "scalaz-core" % scalazV,
    "com.jsuereth" %% "scala-arm" % "2.0",
    "co.fs2" %% "fs2-core" % fs2V,
    "co.fs2" %% "fs2-io" % fs2V,

    "org.clapper" %% "grizzled-slf4j" % "1.3.0"
      exclude("org.slf4j", "slf4j-api"),

    "org.exist-db" % "exist-core" % existV % "provided"
      exclude("org.exist-db.thirdparty.javax.xml.xquery", "xqjapi"),
    "net.sf.saxon" % "Saxon-HE" % "9.6.0-7" % "provided",
    "com.fasterxml.jackson.core" % "jackson-core" % jacksonV % "provided",
    "commons-codec" %	"commons-codec"	% "1.10" % "provided",

    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonV
      exclude("com.fasterxml.jackson.core", "jackson-core"),
    "com.algolia" % "algoliasearch" % algoliaV
      exclude("org.apache.httpcomponents", "*"),
    "com.algolia" % "algoliasearch-common" % algoliaV
      exclude("com.fasterxml.jackson.core", "jackson-core")
      exclude("com.fasterxml.jackson.core", "jackson-databind")
      exclude("org.apache.commons" ,"commons-lang3")
      exclude("org.slf4j", "slf4j-api"),

    "com.typesafe.akka" %% "akka-actor" % akkaV,

    "org.specs2" %% "specs2-core" % "3.8.6" % "test"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers +=
  Resolver.mavenLocal

resolvers +=
  "eXist Maven Repo" at "https://raw.github.com/eXist-db/mvn-repo/master/"
