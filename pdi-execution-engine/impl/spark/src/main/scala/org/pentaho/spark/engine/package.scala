package org.pentaho.spark

import org.pentaho.di.engine.api.{IDataEvent, IProgressReporting}

/**
  * Created by hudak on 12/21/16.
  */
package object engine {

  type EventReport = IProgressReporting[IDataEvent]
}
