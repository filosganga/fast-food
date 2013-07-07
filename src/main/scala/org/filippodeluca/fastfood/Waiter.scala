package org.filippodeluca.fastfood

import akka.actor.{ActorLogging, Actor, ActorRef}
import java.util.UUID

class Waiter(cook: ActorRef) extends Actor with ActorLogging {

  private var orders: Map[UUID, (Order, ActorRef)] = Map.empty

  def receive = {
    case PlaceOrder(id, order, customer) => {

      log.debug("Order " + order + " is received with id " + id)

      Thread.sleep(5000L)

      orders += (id->(order->customer))

      order.required.foreach{x=>
        (0 until x._2).foreach(y=> cook ! Prepare(id, x._1))
      }
    }
    case Ready(id, dish) => {

      val customer = orders(id)._2
      val order = orders(id)._1.ready(dish)

      if(order.isComplete) {
        orders -= id
        log.debug("Order " + id + " is complete")
        customer ! Dishes
      }
      else {
        orders += (id->(order, customer))
      }

    }
  }
}

