package org.pentaho.spark.engine

import org.pentaho.di.trans.{TransHopMeta, TransMeta}

/**
  * Simple transformation consisting of one linear path of steps
  * Created by hudak on 12/16/16.
  */
case class SequentialTransformation(stepDefinitions: Seq[StepDefinition[_]]) {
  val meta: TransMeta = new TransMeta

  // Add each step definition to meta
  for ((stepDef, i) <- stepDefinitions.zipWithIndex) {
    val stepMeta = stepDef.toStepMeta
    stepMeta.setLocation(0, 100 * i)
    meta.addStep(stepMeta)
  }

  // Link sequentially with hops
  for ((from, to) <- meta.getStepsArray zip meta.getStepsArray.drop(1)) {
    meta.addTransHop(new TransHopMeta(from, to))
  }
}

object SequentialTransformation {
  // var-args constructor
  def apply(first: StepDefinition[_], remainder: StepDefinition[_]*): SequentialTransformation =
    SequentialTransformation(first +: remainder)
}
