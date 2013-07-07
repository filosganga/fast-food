package org.filippodeluca.fastfood

import java.util.UUID
import akka.actor.ActorRef

sealed abstract class Message

case object Hello extends Message

case object Goodbye extends Message

case class Prepare(id: UUID, dish: Dish) extends Message

case class Ready(id: UUID, dish: Dish) extends Message

case object Dishes extends Message

case class PlaceOrder(id: UUID, order: Order, customer: ActorRef) extends Message

case object Start extends Message

case object Stop extends Message

case object Generate extends Message

case class NewCustomer(c: ActorRef) extends Message

case class GoToEat(fastFood: ActorRef) extends Message
