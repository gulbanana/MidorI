package actors

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.actor._

//starts up the actor system
class Bootstrapper(checkInterval: FiniteDuration, notifyInterval: FiniteDuration) { this: ActorComponent =>
  def bootstrap() {
    Akka.system.scheduler.schedule(5.seconds, checkInterval, checker, CheckAll())
    Akka.system.scheduler.schedule(5.seconds, notifyInterval, notifier, Notify())
  }
}