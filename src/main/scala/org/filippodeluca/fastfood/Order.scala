package org.filippodeluca.fastfood


case class Order(required: Map[Dish, Int] = Map.empty) {

  def request(dish: Dish) = Order(
    required + (dish->(required.get(dish).getOrElse(0) + 1))
  )

  def ready(dish: Dish) = required.get(dish) match {
    case Some(x) if (x>0) => Order(required + (dish-> (x-1)))
    case _ => throw new RuntimeException("Error: Dish not required")
  }

  def isRequired(dish: String): Boolean = {
    required.exists(x => x._1.equals(dish) && x._2 > 0)
  }

  def isComplete: Boolean = {
    required.forall(x=> x._2 <= 0)
  }
}
