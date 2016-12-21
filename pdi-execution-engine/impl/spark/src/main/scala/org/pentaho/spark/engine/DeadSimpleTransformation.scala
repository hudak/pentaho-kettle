package org.pentaho.spark.engine

import java.util.concurrent.TimeUnit

import org.apache.spark.SparkConf
import org.pentaho.di.core.KettleEnvironment
import org.pentaho.di.engine.kettlenative.impl.Transformation
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.steps.datagrid.DataGridMeta
import org.pentaho.di.trans.steps.writetolog.WriteToLogMeta

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by hudak on 12/14/16.
  */
object DeadSimpleTransformation extends App {
  KettleEnvironment.init()

  private val fields = Array("ID, Name")

  val input: StepDefinition[DataGridMeta] = StepDefinition("Data Grid", new DataGridMeta) { meta =>
    meta.allocate(2)
    meta.setFieldName(fields)
    meta.setFieldType(Array("Integer", "String"))

    val dataLines = for {
      id <- 0 to 100
      name = Random.alphanumeric.take(10).mkString
    } yield List(id.toString, name).asJava
    meta.setDataLines(dataLines.asJava)
  }

  val output: StepDefinition[WriteToLogMeta] = StepDefinition("Logging", new WriteToLogMeta) { meta =>
    meta.setFieldName(fields)
  }

  val transformation = SequentialTransformation(input, output)

  val trans = new Trans(transformation.meta)
  //    trans.prepareExecution(null)
  //    trans.startThreads()
  //
  //    trans.waitUntilFinished()

  val nativeTrans = Transformation.convert(transformation.meta)

  private val conf = new SparkConf().setAppName("Simple Transformation").setMaster("local")
  val engine = new SparkEngine(conf, NativeKettleOperation.compiler(), Wiring.default())

  val result = engine.execute(nativeTrans).get(20, TimeUnit.SECONDS)

  result.getDataEventReport.asScala foreach (println(_))
}
