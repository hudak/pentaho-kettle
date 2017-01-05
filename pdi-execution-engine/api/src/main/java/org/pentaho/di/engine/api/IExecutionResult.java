package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.IProgressReporting;

/**
 * Created by nbaker on 6/22/16.
 */
public interface IExecutionResult extends IProgressReporting {
  Status getStatus();

  ITransformation getTransformation();

  IEngine getEngine();
}
