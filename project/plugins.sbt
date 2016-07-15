resolvers ++= Seq(
	"Maven central" at "http://repo1.maven.org/maven2/",
	"Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
	"Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.1")
