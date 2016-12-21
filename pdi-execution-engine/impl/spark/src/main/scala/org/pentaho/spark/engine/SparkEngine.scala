package org.pentaho.spark.engine

import java.util.concurrent.{CompletableFuture, Future => JFuture}

import com.google.common.collect.ImmutableList
import org.apache.spark.{SparkConf, SparkContext}
import org.pentaho.di.core.KettleEnvironment
import org.pentaho.di.engine.api._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by hudak on 12/19/16.
  */
class SparkEngine(conf: SparkConf, compiler: Operation.Compiler, wiring: Wiring) extends IEngine {
  if (!KettleEnvironment.isInitialized) KettleEnvironment.init()

  override def execute(trans: ITransformation): JFuture[IExecutionResult] = {
    val execution = ContextImpl(new SparkContext(conf), trans).materialize()

    // Wire operations
    val wire = wiring(execution)(_, _)
    for {
      hop <- trans.getHops.asScala
      from = execution.lookup(hop.getFrom.getId)
      to = execution.lookup(hop.getTo.getId)
    } wire(from, to)

    // Let's kick this pig
    execution.operations foreach (_.start())

    // Aggregate results
    val execFuture = new CompletableFuture[IExecutionResult]

    Future.sequence(execution.operations map (_.report)) andThen {
      case Success(result) => execFuture complete new IExecutionResult {
        override def getDataEventReport: ImmutableList[EventReport] = ImmutableList.copyOf(result.toArray)
      }
      case Failure(t) => execFuture completeExceptionally t
    }

    execFuture
  }

  case class ContextImpl(spark: SparkContext, transformation: ITransformation) extends ExecutionContext {
    def materialize(): Execution = {
      val compile = compiler(this).lift

      val operations = for {
        iOp <- transformation.getOperations.asScala
      } yield compile(iOp) getOrElse sys.error(s"Unable to compile $iOp")

      ExecutionImpl(this, operations)
    }
  }

  case class ExecutionImpl(context: ExecutionContext, operations: Seq[Operation]) extends Execution {
    private val byId = for {
      (id, ops) <- operations groupBy (_.id)
      op <- ops
    } yield id -> op

    override def lookup(id: String): Operation = byId(id)

    override val spark: SparkContext = context.spark
    override val transformation: ITransformation = context.transformation
  }

}
