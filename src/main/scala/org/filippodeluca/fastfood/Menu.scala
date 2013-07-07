package org.filippodeluca.fastfood

import scala.concurrent.duration.Duration

case class Menu(dishes: Set[Dish]) extends Iterable[Dish] {

  def apply(name: String): Dish = dishes.find(d=> d.name == name).get

  def iterator = dishes.iterator

}

case class Dish(name: String, cookingTime: Duration)
