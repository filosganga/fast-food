/**
 * Copyright 2012 Filippo De Luca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.filippodeluca.fastfood

import akka.actor._
import java.util.UUID
import akka.routing.RoundRobinRouter
import akka.util.Duration
import scala.Some

object Main extends App {

  sealed abstract class Message

  case object Hello extends Message

  case object Goodbye extends Message

  case class Menu(dishes: Set[String]) extends Message

  case class Prepare(name: String) extends Message

  case class Ready(name: String) extends Message

  case object Dishes extends Message

  case class PlaceOrder(id: UUID, order: Order, customer: ActorRef) extends Message

  case object Start extends Message

  case object Stop extends Message

  case object Generate extends Message


  case class Order(required: Map[String, Int] = Map.empty) {

    def request(dish: String) = Order(required + (dish->(required.get(dish).getOrElse(0) + 1)))

    def ready(dish: String) = required.get(dish) match {
      case Some(x) if(x>0) => Order(required + (dish-> (x-1)))
      case _ => throw new RuntimeException("ERROR")
    }

    def isRequired(dish: String): Boolean = {
      required.find(x=> x._1.equals(dish) && x._2 > 0).isDefined
    }

    def isComplete: Boolean = {
      required.forall(x=> x._2 <= 0)
    }
  }

  class Cook(dishes: Map[String, Long]) extends Actor with ActorLogging {

    protected def receive = {

      case Prepare(dish) => {
        log.debug("Preparing " + dish + "...")
        Thread.sleep(dishes(dish))
        sender ! Ready(dish)
      }
    }
  }

  class Waiter(cook: ActorRef) extends Actor with ActorLogging {

    private var orders: Map[UUID, (Order,ActorRef)] = Map.empty

    protected def receive = {
      case PlaceOrder(id, order, customer) => {

        log.debug("Order " + order + " is received with id " + id)

        Thread.sleep(5000L)

        orders += (id->(order->customer))

        order.required.foreach{x=>
          (0 until x._2).foreach(y=> cook ! Prepare(x._1))
        }
      }
      case Ready(dish) => {

        orders.find(entry=> entry._2._1.isRequired(dish)) match {
          case Some((id, (order, customer))) => {
            val updated = order.ready(dish)
            if (updated.isComplete) {
              orders -= id
              log.debug("Order " + id + " is complete")
              customer ! Dishes
            }
            else {
              orders += (id->(updated->customer))
            }
          }
          case None => log.error("The cook prepared a non needed dish: " + dish)
        }
      }
    }
  }

  class Cashier(waiter: ActorRef) extends Actor with ActorLogging {

    protected def receive = {
      case order:Order => {

        log.debug("Registering order ...")

        Thread.sleep(5000L)

        val id = UUID.randomUUID()
        waiter ! PlaceOrder(id, order, sender)
      }
    }
  }

  class Customer extends Actor with ActorLogging {

    val fastFood = context.actorFor("/user/fast-food")
    val cashier = context.actorFor("/user/fast-food/cashiers")

    fastFood ! Hello

    protected def receive = {
      case Menu(dishes) => {

        log.debug("Thinking what to order...")

        Thread.sleep(5000L)

        val noOfDishes = (math.random * 4 + 1).toInt
        val toOrder = (0 until noOfDishes).map(x=> dishes.toSeq((math.random * dishes.size).toInt))

        cashier ! Order(toOrder.groupBy(x=>x).mapValues(_.size))
      }
      case Dishes => {

        log.debug("Eating...")

        Thread.sleep(5000L)

        fastFood ! Goodbye

        context.stop(self)
      }
    }
  }

  class FastFood(menu: Map[String, Long]) extends Actor with ActorLogging {

    var customers = 0

    val noOfCooks = 5
    val noOfWaiters = 1
    val noOfCashiers = 1

    val cooks = context.actorOf(Props(new Cook(menu)).withRouter(RoundRobinRouter(noOfCooks)), name = "cooks")
    val waiters = context.actorOf(Props(new Waiter(cooks)).withRouter(RoundRobinRouter(noOfWaiters)), name = "waiters")
    val cashiers = context.actorOf(Props(new Cashier(waiters)).withRouter(RoundRobinRouter(noOfCashiers)), name = "cashiers")

    protected def receive = {
      case Hello => {
        customers += 1
        log.debug("Customers: " + customers)
        sender ! Menu(menu.keys.toSet)
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

  class CustomerGenerator extends Actor with ActorLogging {

    val fastFood = context.actorFor("/user/fast-food")

    var counter = 0

    protected def receive = {
      case Generate => {
        val name = "customer-" + counter
        context.actorOf(Props[Customer], name = name)
        log.debug("Customer " + name + " generated")
        counter += 1
      }
    }

  }


  val menu = Map(
    "Spaghetti"->5000L,
    "Bistecca"->3000L,
    "Bruschette"-> 2000L,
    "Pesce"->8000L,
    "Faggioli"->9000L,
    "Zuppa"->10000L,
    "Frullato"->1000L
  )

  val system = ActorSystem("FastFoodSystem")

  val fastFood = system.actorOf(Props(new FastFood(menu)), name = "fast-food")

  val customerGenerator = system.actorOf(Props[CustomerGenerator], name = "customer-generator")

  fastFood ! Start

  system.scheduler.schedule(Duration("2s"), Duration("5s")){
    customerGenerator ! Generate
  }

  Thread.sleep(30000L)

  fastFood ! Stop

  system.shutdown()

}



