name := "meet-what-you-tweet"
description := "A demo application to showcase identifying topics from tweets"
version  := "0.1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
	"edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
	"org.scalatest" %% "scalatest" % "2.2.6" % "test",
	"org.carrot2" % "carrot2-core" % "3.13.0",
	"com.danielasfregola" %% "twitter4s" % "0.2",
	"commons-logging" % "commons-logging" % "1.2"
)