package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.Metrics;

import java.util.Map;

/**
 * Created by nbaker on 6/22/16.
 */
public interface IExecutionResult {
  IExecutionContext getContext();

  Map<IOperation, Metrics> getDataEventReport();
}
