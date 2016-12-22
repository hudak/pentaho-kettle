package org.pentaho.spark

import java.util.concurrent.TimeUnit

import org.apache.spark.SparkConf
import org.pentaho.di.core.KettleEnvironment
import org.pentaho.di.engine.kettlenative.impl.Transformation
import org.pentaho.di.trans.TransMeta
import org.pentaho.spark.engine.{NativeKettleOperation, SparkEngine, Wiring}

import scala.collection.JavaConverters._

/**
  * Created by hudak on 12/21/16.
  */
object RunKTR extends App {
  KettleEnvironment.init()

  val transMeta = new TransMeta(args(0))
  val nativeTrans = Transformation.convert(transMeta)

  private val conf = new SparkConf().setAppName("Simple Transformation").setMaster("local")
  val engine = new SparkEngine(conf, NativeKettleOperation.compiler(), Wiring.default())

  val result = engine.execute(nativeTrans).get(20, TimeUnit.SECONDS)

  result.getDataEventReport.asScala foreach (println(_))
}
