package org.filippodeluca.fastfood

import akka.actor.{ActorRef, ActorLogging, Actor}

class Customer extends Actor with ActorLogging {

  var fastFood: Option[ActorRef] = None

  def receive = {
    case GoToEat(ff) => {
      fastFood = Some(ff)

      ff ! Hello
    }
    case Menu(dishes) => {

      log.debug("Thinking what to order...")

      Thread.sleep(5000L)

      val noOfDishes = (math.random * 4 + 1).toInt
      val toOrder = (0 until noOfDishes).map(x=> dishes.toSeq((math.random * dishes.size).toInt))

      sender ! Order(toOrder.groupBy(x=>x).mapValues(_.size))
    }
    case Dishes => {

      log.debug("Eating...")

      Thread.sleep(5000L)

      fastFood.foreach(ff=> ff ! Goodbye)

      context.stop(self)
    }
  }
}
