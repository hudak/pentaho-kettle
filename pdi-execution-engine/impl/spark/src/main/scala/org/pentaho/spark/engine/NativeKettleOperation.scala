package org.pentaho.spark.engine

import org.pentaho.di.engine.api.{IDataEvent, IOperation, ITransformation}
import org.pentaho.di.engine.kettlenative.impl.{KettleDataEvent, KettleExecOperation}
import org.reactivestreams.{Subscriber, Subscription}

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  * Created by hudak on 12/21/16.
  */
class NativeKettleOperation(op: IOperation, trans: ITransformation)
  extends KettleExecOperation(op, trans, ExecutionContext.global)
    with Operation {

  override val id: String = getId

  // Create a promise for completion
  private val complete = Promise[EventReport]()
  subscribe(new Subscriber[IDataEvent] {
    override def onSubscribe(s: Subscription): Unit = ()

    override def onNext(t: IDataEvent): Unit = ()

    override def onComplete(): Unit = complete success NativeKettleOperation.this

    override def onError(t: Throwable): Unit = complete failure t
  })

  override def start(): Unit = {
    // If step has no inputs, trigger startup with an empty event
    if (getHopsIn.isEmpty) {
      onNext(KettleDataEvent.empty())
    }
  }

  override def report: Future[EventReport] = complete.future
}

object NativeKettleOperation {
  /**
    * Accepts any operation and creates a native kettle transformation to run from the driver.
    *
    * This compiler accepts any IOperation and should be used as a last resort
    */
  def compiler(): Operation.Compiler = execution => {
    case op => new NativeKettleOperation(op, execution.transformation)
  }
}
