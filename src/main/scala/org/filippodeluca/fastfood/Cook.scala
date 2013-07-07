package org.filippodeluca.fastfood

import akka.actor.{ActorLogging, Actor}

class Cook(dishes: Menu) extends Actor with ActorLogging {

  def receive = {

    case Prepare(id, dish) => {
      log.debug("Preparing " + dish + " for order " + id)
      Thread.sleep(dish.cookingTime.toMillis)
      sender ! Ready(id, dish)
    }
  }
}
