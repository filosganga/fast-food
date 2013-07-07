package org.filippodeluca.fastfood

import akka.actor.{Props, ActorLogging, Actor}
import akka.routing.RoundRobinRouter

class FastFood(menu: Menu) extends Actor with ActorLogging {

  var customers = 0

  val noOfCooks = 5
  val noOfWaiters = 1
  val noOfCashiers = 1

  val cooks = context.actorOf(Props(new Cook(menu)).withRouter(RoundRobinRouter(noOfCooks)), name = "cooks")
  val waiters = context.actorOf(Props(new Waiter(cooks)).withRouter(RoundRobinRouter(noOfWaiters)), name = "waiters")
  val cashiers = context.actorOf(Props(new Cashier(menu, waiters)).withRouter(RoundRobinRouter(noOfCashiers)), name = "cashiers")

  def receive = {
    case Hello => {
      customers += 1
      log.debug("Customers: " + customers)
      cashiers.tell(Hello, sender)
    }
    case Goodbye => {
      customers -= 1
      log.debug("Customers: " + customers)
    }
    case Start => {
      log.debug("Fast food opened")
    }
    case Stop => {
      log.debug("Fast food closed")
    }
  }
}

