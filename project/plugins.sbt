logLevel := Level.Warn

resolvers ++= Seq(
  "dnvriend" at "http://dl.bintray.com/dnvriend/maven"//,
  //"maven2" at "http://central.maven.org/maven2"
)
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.34")
//addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")
