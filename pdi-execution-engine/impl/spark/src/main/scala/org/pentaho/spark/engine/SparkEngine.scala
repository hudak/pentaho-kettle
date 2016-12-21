package org.pentaho.spark.engine

import java.util.concurrent.{CompletableFuture, Future => JFuture}

import com.google.common.collect.ImmutableList
import org.pentaho.di.core.KettleEnvironment
import org.pentaho.di.engine.api._
import org.pentaho.di.engine.kettlenative.impl.{KettleDataEvent, KettleExecOperation}
import org.reactivestreams.{Subscriber, Subscription}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by hudak on 12/19/16.
  */
class SparkEngine extends IEngine {
  if (!KettleEnvironment.isInitialized) KettleEnvironment.init()

  type EventReporting = IProgressReporting[IDataEvent]

  override def execute(trans: ITransformation): JFuture[IExecutionResult] = {
    // Compile operations
    val execOperations = trans.getOperations.asScala flatMap compileOperation(trans) toMap

    // Wire up exec ops
    for {
      to <- execOperations.values
      hop <- to.getHopsIn.asScala
      from = execOperations(hop.getFrom.getId)
    } from.subscribe(to)

    // Subscribe to operations
    val reports = for {
      exOp <- execOperations.values
      subscriber = OperationSubscriber(exOp)
    } yield subscriber.promise.future

    // Let's kick this pig
    trans.getSourceOperations.asScala map (_.getId) map execOperations foreach (_.onNext(KettleDataEvent.empty()))

    // Aggregate results
    val execFuture = new CompletableFuture[IExecutionResult]

    Future.sequence(reports) andThen {
      case Success(result) => execFuture complete new IExecutionResult {
        override def getDataEventReport: ImmutableList[EventReporting] = ImmutableList.copyOf(result.asJava)
      }
      case Failure(t) => execFuture completeExceptionally t
    }

    execFuture
  }

  private def compileOperation(trans: ITransformation)(operation: IOperation): Map[String, IExecutableOperation] = {
    Map(operation.getId -> KettleExecOperation.compile(operation, trans, global))
  }

  private case class OperationSubscriber(reporting: EventReporting) extends Subscriber[IDataEvent] {
    val promise: Promise[EventReporting] = Promise[EventReporting]()
    reporting.subscribe(this)

    override def onSubscribe(s: Subscription): Unit = ()

    override def onNext(t: IDataEvent): Unit = ()

    override def onComplete(): Unit = promise success reporting

    override def onError(t: Throwable): Unit = promise failure t
  }

}
