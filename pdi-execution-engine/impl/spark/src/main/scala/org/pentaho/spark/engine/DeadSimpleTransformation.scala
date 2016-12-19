package org.pentaho.spark.engine

import org.pentaho.di.core.KettleEnvironment
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.steps.datagrid.DataGridMeta
import org.pentaho.di.trans.steps.writetolog.WriteToLogMeta

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Created by hudak on 12/14/16.
  */
object DeadSimpleTransformation {
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

  def main(args: Array[String]): Unit = {
    KettleEnvironment.init()

    val trans = new Trans(transformation.meta)
    trans.prepareExecution(null)
    trans.startThreads()

    trans.waitUntilFinished()
  }
}
