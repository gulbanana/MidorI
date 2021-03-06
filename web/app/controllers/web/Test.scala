package controllers.web

import scala.concurrent._
import java.net.URL
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.joda.time._
import models._
import dal._

class Test extends Controller { this: DAOComponent =>
  def delete = Action { Async { 
    deleteImpl.map(Ok(_))
  }}
  
  def deleteImpl = {
    for (
      droppedUsers <- dao.deleteAllUsers();
      droppedFeeds <- dao.deleteAllFeeds()
    ) yield {
      if (droppedUsers && droppedFeeds) {
        "Deleted MidorI database."
      } else {
        "Failed to delete MidorI database."
      }
    }
  }
  
  def create = Action {
    Async {
      createImpl.map(Ok(_))
    }
  }
  
  def createImpl = {      
    val insertions = Seq(
	  dao.createFeed(new URL("http://www.mspaintadventures.com/rss/rss.xml")),
	  dao.createFeed(new URL("http://feeds.feedburner.com/JonSkeetCodingBlog?format=xml")),
	  dao.createFeed(new URL("http://badmachinery.com/index.xml")),
	  dao.createFeed(new URL("http://cucumber.gigidigi.com/feed/")),
	  dao.createFeed(new URL("http://www.doublefine.com/dfa/dfarss/")),
	  dao.createFeed(new URL("http://www.rsspect.com/rss/gunner.xml")),
	  dao.createFeed(new URL("http://feeds.feedburner.com/Introversion_blog?format=xml")),
	  dao.createFeed(new URL("http://nedroid.com/feed/")),
	  dao.createFeed(new URL("http://nonadventures.com/feed/")),
	  dao.createFeed(new URL("http://the-witness.net/news/feed/")),
	  dao.createFeed(new URL("https://issues.scala-lang.org/activity?maxResults=20&streams=user+IS+den%5C_sh&title=Denys%20Shabalyn")),
      dao.createUser(User(
	    "banana", 
	      Seq(
	        Subscription(new URL("http://www.mspaintadventures.com/rss/rss.xml"), Seq()),
	        Subscription(new URL("http://feeds.feedburner.com/JonSkeetCodingBlog?format=xml"), Seq()),
	        Subscription(new URL("http://badmachinery.com/index.xml"), Seq()),
	        Subscription(new URL("http://badmachinery.com/index.xml"), Seq()),
	        Subscription(new URL("http://cucumber.gigidigi.com/feed/"), Seq()),
	        Subscription(new URL("http://www.doublefine.com/dfa/dfarss/"), Seq()),
	        Subscription(new URL("http://www.rsspect.com/rss/gunner.xml"), Seq()),
	        Subscription(new URL("http://feeds.feedburner.com/Introversion_blog?format=xml"), Seq()),
	        Subscription(new URL("http://nedroid.com/feed/"), Seq()),
	        Subscription(new URL("http://nonadventures.com/feed/"), Seq()),
	        Subscription(new URL("http://the-witness.net/news/feed/"), Seq()),
	        Subscription(new URL("https://issues.scala-lang.org/activity?maxResults=20&streams=user+IS+den%5C_sh&title=Denys%20Shabalin"), Seq())
	      )
	    ))
    )
      
    Future.reduce(insertions) { _ && _ } map { 
      if (_) { 
	      "Created MidorI database."
	    } else {
	      "Failed to create MidorI database."
	    }
    }
  }
  
  def reset = Action { Async {
    Future.sequence(Seq(deleteImpl, createImpl)).map(futures => Ok(futures(0) + "\n" + futures(1)))
  }}
}
