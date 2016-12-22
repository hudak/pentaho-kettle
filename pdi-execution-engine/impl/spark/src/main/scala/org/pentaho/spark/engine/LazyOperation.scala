package org.pentaho.spark.engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait LazyOperation[T] extends Operation with LazySource[T] with LazyResult[T] {

  def define(inputs: Map[String, T]): Map[String, T]

  inputReady andThen {
    case Success(input) =>
      val output = define(input)
      for ((id, result) <- output) {
        setResult(id, Future.successful(result))
      }
      for (expected <- expectedResults if !output.contains(expected)) {
        setResult(expected, Future.failed(new RuntimeException(s"Expected output ($expected) was not defined")))
      }
    case Failure(t) =>
      for (expected <- expectedResults) {
        setResult(expected, Future.failed(new RuntimeException(s"Failure with expected input", t)))
      }
  }
}

trait LazySource[T] extends Operation {
  val expectedSources: Seq[String]

  private val inputs: Map[String, Promise[T]] = expectedSources.map(id => id -> Promise[T]()).toMap

  protected val inputReady: Future[Map[String, T]] = {
    val futures = for ((id, promise) <- inputs) yield promise.future map (id -> _)
    Future.sequence(futures) map (_.toMap)
  }

  def setSource(id: String, value: Future[T]): Unit = inputs(id).completeWith(value)

  def setSource(id: String, value: => T): Unit = setSource(id, Future(value))
}

trait LazyResult[T] extends Operation {
  def subscribe(to: LazySource[T]): Unit = to.setSource(id, getResult(to.id))

  val expectedResults: Seq[String]

  private val outputs = expectedResults.map(id => id -> Promise[T]()).toMap

  def setResult(id: String, value: Future[T]): Unit = outputs(id).completeWith(value)

  def getResult(id: String): Future[T] = outputs(id).future
}

