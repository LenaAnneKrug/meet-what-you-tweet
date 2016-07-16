package com.whimsicalbees.mwyt

import com.danielasfregola.twitter4s.entities.enums.Measure
import com.danielasfregola.twitter4s.entities.{Accuracy, GeoCode, LocationTrends, Tweet}
import com.whimsicalbees.twitter.TwitterHelper
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm
import org.carrot2.core.{ControllerFactory, Document}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.ceil
import scala.util.{Failure, Success}

object TweetWhatYouMeet extends App {
  val cKey = sys.env("TWITTER_CONSUMER_KEY")
  val cSecret = sys.env("TWITTER_CONSUMER_SECRET")

  val aKey = sys.env("TWITTER_ACCESS_KEY")
  val aSecret = sys.env("TWITTER_ACCESS_SECRET")

  val twitter = TwitterHelper(cKey, cSecret, aKey, aSecret)
  val woeid_nyc = 2459115
  //Hard coded this but it is based on Yahoo!s Where On Earth Identifier (https://en.wikipedia.org/wiki/WOEID)
  val nyc_geocode = GeoCode.apply(40.7259940, -73.9957090, new Accuracy(8000, Measure.Meter))
  //Hard Coded for experiment
  val tweetFetchLimit = 3200 //3200 is the max per request set by the Twitter API

  /*
   * Gets trends based on a hard coded WOEID of NYC and process them based on the provided function.
   */
  def doTrends(p: LocationTrends => Unit) {

    twitter.getTrends(woeid_nyc).onSuccess {
      case trends => p(trends)
    }
  }

  /*
   * Processes the top 10 trends
   * Essentially searches for the most recent tweets based on the top 10 trends and
   * takes the first 25 unique users and gets there recent tweets and process them using a Lingo Clustering Algorithm to determine
   * Categories
   */
  def processTrends(trends: LocationTrends) = {

    Future.sequence(trends.trends.subList(0, 10).map(trend => doSearch(trend.name))).onComplete {

      case Success(results) =>
        val users = usersFromTweets(results.flatMap(result => result.statuses)).take(25)

        Future.sequence(users.map(user => userTimeline(user))).onComplete {
          case Success(tweets) =>
            tweets.filter(t => t.nonEmpty).foreach(t => {
              println(s"USER: ${t.head.user.get.screen_name}")
              processTopics(t.map(tweet => tweet.text))
            })
          case Failure(t) => println("An error has occurred: " + t.getMessage)
        }

      case Failure(t) => println("An error has occurred: " + t.getMessage)
    }
  }

  /*
   * Searches for tweets based on a query
   */
  def doSearch(search: String) = {
    twitter.searchTweet(query = search, count = tweetFetchLimit)
  }

  /*
   * Retrieves a users recent timeline
   */
  def userTimeline(user: String) = {
    twitter.getUserTimelineForUser(screen_name = user, count = tweetFetchLimit, exclude_replies = true, include_rts = false)
  }

  /*
   * Determines the unique users from a list of tweets
   */
  def usersFromTweets(tweets: Seq[Tweet]) = {

    tweets.filter(_.user.isDefined).map(tweet => tweet.user.get.screen_name).distinct
  }

  /*
   * Processes tweets using a Lingo Clustering Algorithm
   */
  def processTopics(topics: Seq[String]) {
    val controller = ControllerFactory.createSimple()
    val documents = topics.map(new Document(_))
    val clustersByTopic = controller.process(documents, null, classOf[LingoClusteringAlgorithm])
    clustersByTopic.getClusters.sortWith(_.getScore < _.getScore).foreach(c => {
      println(s"${ceil(c.getScore)} : ${c.getPhrases} : ${c.getLabel}")
    })
  }

  //Start processing, output goes to stdout
  doTrends(processTrends)
}