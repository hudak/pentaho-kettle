package org.pentaho.spark

import org.apache.spark.rdd.RDD
import org.pentaho.di.engine.api.{IData, IDataEvent, IProgressReporting}

/**
  * Created by hudak on 12/21/16.
  */
package object engine {

  type EventReport = IProgressReporting[IDataEvent]
  type KettleRowRDD = RDD[IData[_]]
}
