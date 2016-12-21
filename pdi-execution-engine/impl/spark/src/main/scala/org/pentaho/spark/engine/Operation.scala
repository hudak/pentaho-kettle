package org.pentaho.spark.engine

import org.pentaho.di.engine.api.IOperation

import scala.concurrent.Future

/**
  * Created by hudak on 12/21/16.
  */
trait Operation {
  val id: String

  def start(): Unit

  def report: Future[EventReport]
}

object Operation {
  type Compiler = ExecutionContext => PartialFunction[IOperation, Operation]

  private val NONE = PartialFunction.empty[IOperation, Operation]

  /**
    * Merge many compilers into one
    *
    * @param compilers sequence of compilers to merge
    * @return a single compiler that will attempt each compiler sequentially until one returns an Operation
    */
  def merge(compilers: Seq[Compiler]): Compiler = execution => {
    val partialFunctions = compilers map (_ (execution))
    (NONE /: partialFunctions) (_ orElse _)
  }
}
