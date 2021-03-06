package rss

import scala.collection.JavaConversions._
import java.net.URL
import play.api._
import com.sun.syndication.io._
import com.sun.syndication.feed.synd._
import com.sun.syndication.fetcher.impl._
import org.joda.time._
import models._

trait ROMERSSComponent extends RSSComponent {
  val source = new ROMESource()
  
  class ROMESource extends FeedSource {
    val fetcher = new HttpClientFeedFetcher(new HashMapFeedInfoCache()) //XXX replace this with a custom cache, db-backed or created from the feed
    val ttl = Duration.standardHours(1) //XXX adapt this to the feed
    
    def retrieve(url: URL, lastCheck: DateTime) = {
      Logger.debug("retrieving uri %s, last checked at %s".format(url, lastCheck.toString()))
      
      val feed = fetcher.retrieveFeed(url)
      val title = feed.getTitle()
      val description = Option(feed.getDescription()).getOrElse("")
      val entries = feed.getEntries().map(_.asInstanceOf[SyndEntry])
      val pubDate = new DateTime(if (feed.getPublishedDate != null) feed.getPublishedDate else entries.head.getPublishedDate)
      val link = Option(feed.getLink()).getOrElse(feed.getUri())
      
      if (pubDate.isAfter(lastCheck)) {
        Logger.debug("feed '%s' updated at %s".format(title, pubDate.toString()))
        val lastUpdate = DateTime.now
        val nextUpdate = lastUpdate.plus(ttl)
        val updatedFeed = Feed(
          FeedInfo(
	        url, 
	        title, 
	        description, 
	        new URL(link), 
	        lastUpdate, 
	        nextUpdate
          ),
          entries.map(asEntry)
        )
        Some(updatedFeed)
      } else {
        Logger.debug("feed '%s' not updated since %s".format(title, pubDate.toString()))
        None
      }
    }
    
    private def asEntry(item: SyndEntry) = Entry(
      new DateTime(item.getPublishedDate()), 
      item.getTitle(), 
      new URL(item.getLink()), 
      Option(item.getDescription()).map(_.getValue()).getOrElse("")
    ) 
  }
}