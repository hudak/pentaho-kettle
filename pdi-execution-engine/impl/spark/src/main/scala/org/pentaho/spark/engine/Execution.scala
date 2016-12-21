package org.pentaho.spark.engine

import org.apache.spark.SparkContext
import org.pentaho.di.engine.api.ITransformation

/**
  * Created by hudak on 12/21/16.
  */
trait Execution extends ExecutionContext {
  val operations: Seq[Operation]

  def lookup(id: String): Operation
}

trait ExecutionContext {
  val spark: SparkContext
  val transformation: ITransformation
}

