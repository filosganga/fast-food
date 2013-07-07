package org.filippodeluca.fastfood

import akka.actor.{ActorLogging, Actor, ActorRef}
import java.util.UUID

class Cashier(menu: Menu, waiter: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case Hello => {
      sender ! menu
    }
    case order:Order => {

      log.debug("Registering order ...")

      Thread.sleep(5000L)

      val id = UUID.randomUUID()
      waiter ! PlaceOrder(id, order, sender)
    }
  }
}