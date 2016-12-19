package org.pentaho.spark.engine

import org.pentaho.di.trans.step.{StepMeta, StepMetaInterface}

/**
  * Functional definition of a step
  * Created by hudak on 12/19/16.
  */
case class StepDefinition[T <: StepMetaInterface](name: String, getMetaInterface: () => T) {
  def toStepMeta: StepMeta = new StepMeta(name, getMetaInterface())
}

object StepDefinition {
  def apply[T <: StepMetaInterface](name: String, smi: => T)(config: T => Unit): StepDefinition[T] =
    StepDefinition(name, metaInterfaceFactory(smi, config))

  private def metaInterfaceFactory[T <: StepMetaInterface](smi: => T, config: (T) => Unit)() = {
    val meta = smi
    config(meta)
    meta
  }
}