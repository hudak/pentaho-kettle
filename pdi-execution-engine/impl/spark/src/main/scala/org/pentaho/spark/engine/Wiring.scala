package org.pentaho.spark.engine

import org.pentaho.di.engine.api.IDataEvent
import org.reactivestreams.{Publisher, Subscriber}

/**
  * Created by hudak on 12/21/16.
  */
trait Wiring {
  def apply(execution: Execution)(from: Operation, to: Operation)
}

object Wiring {
  def apply(wiring: (Execution, Operation, Operation) => Unit): Wiring = new Wiring {
    override def apply(execution: Execution)(from: Operation, to: Operation): Unit = wiring(execution, from, to)
  }

  def default() = Wiring { (execution, from, to) =>
    (from, to) match {
      case (from: Publisher[IDataEvent], to: Subscriber[IDataEvent]) => from.subscribe(to)
      case (_, _) => sys.error(s"Unable to wire $from to $to")
    }
  }
}
