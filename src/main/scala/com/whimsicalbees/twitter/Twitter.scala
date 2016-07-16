package com.whimsicalbees.twitter

import com.danielasfregola.twitter4s.entities.{ ConsumerToken, AccessToken, Tweet, HashTag }
import com.danielasfregola.twitter4s.TwitterClient

object TwitterHelper {

	def apply(consumerKey: String, consumerSecret: String, accessKey: String, accessSecret: String) = {

		new TwitterClient(
			ConsumerToken(key = consumerKey, secret = consumerSecret),
			AccessToken(key = accessKey, secret = accessSecret))
	}

	def getTopHashtags(tweets: Seq[Tweet], n: Int = 10): Seq[(String, Int)] = {

		val hashtags: Seq[Seq[HashTag]] = tweets.map { tweet =>
			tweet.entities.map(_.hashtags).getOrElse(Seq.empty)
		}
		val hashtagTexts: Seq[String] = hashtags.flatten.map(_.text.toLowerCase)
		val hashtagFrequencies: Map[String, Int] = hashtagTexts.groupBy(identity).mapValues(_.size)

		hashtagFrequencies.toSeq.sortBy { case (entity, frequency) => -frequency }.take(n)
	}
}