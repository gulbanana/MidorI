package controllers.api

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api._
import play.api.mvc._
import org.joda.time._
import models._
import dal._
import actors._
import controllers.Authenticator

class News extends Controller { this: DAOComponent with ActorComponent with Authenticator => 
  private val pageSize = 15
  
  def get = Action { 
    Async { 
      Authenticated { implicit user => 
        for (
          items <- paginatedItems(0, pageSize);
          marked <- markRead(items)
        ) yield Ok(views.html.index(user,items))
      }
    }
  }
  
  def set = Action(parse.json) { request =>
    request.body.validate[Int].map {
      case start => Async { 
        Authenticated { implicit user => 
          for (
            items <- paginatedItems(start, pageSize);
            marked <- markRead(items)
          ) yield Ok(views.html.items(items))
        }
      } 
    }.recoverTotal { error =>
      BadRequest("Invalid request body.")
    }
  }
  
  private def paginatedItems(skip: Int, take: Int)(implicit user: User) = for (feeds <- dao.getSubscribedFeeds(user)) yield feeds
    .filter(feed => feed.entries.isDefined)
    .flatMap(feed => feed.entries.get.map(entry => Item(entry, feed, user.subscriptions.filter(sub => sub.feed==feed.url).flatMap(_.entries).contains(entry.link.toString))))
    .sorted(Ordering.by[Item,Long](item => item.entry.posted.getMillis).reverse)
    .drop(skip)
    .take(take)
    
  private def markRead(items: Seq[Item])(implicit user: User) = Future
    .traverse(items.filter(item => !item.read))(item => dao.markRead(user, item))
    .map(futures => futures.fold(true)(_ && _))
}