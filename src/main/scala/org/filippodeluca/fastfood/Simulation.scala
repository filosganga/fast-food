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
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory


object Simulation extends App {


  class CustomerGenerator extends Actor with ActorLogging {

    val fastFood = context.actorFor("/user/fast-food")

    var counter = 0

    def receive = {
      case Generate => {
        val name = "customer-" + counter
        val customer = context.actorOf(Props[Customer], name = name)
        log.debug("Customer " + name + " generated")
        counter += 1

        customer ! GoToEat(fastFood)
      }
    }

  }

  val config = ConfigFactory.load()

  LoggerFactory.getLogger("Foo").info("Starting...")

  val dishes = Map(
    "Spaghetti"->5000L,
    "Bistecca"->3000L,
    "Bruschette"-> 2000L,
    "Pesce"->8000L,
    "Faggioli"->9000L,
    "Zuppa"->10000L,
    "Frullato"->1000L
  )

  val menu = Menu(dishes.map{case(n, t)=> Dish(n, Duration(t, "ms"))}.toSet)

  val system = ActorSystem("FastFoodSystem")

  val fastFood = system.actorOf(Props(new FastFood(menu)), name = "fast-food")

  val customerGenerator = system.actorOf(Props[CustomerGenerator], name = "customer-generator")

  fastFood ! Start

  import system.dispatcher
  system.scheduler.schedule(2 seconds, 5 seconds) {
    customerGenerator ! Generate
  }

  Thread.sleep((45 seconds).toMillis)

  fastFood ! Stop

  system.shutdown()

}



