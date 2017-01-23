package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.IReportingEventSource;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by hudak on 1/18/17.
 */
public interface EngineProxy {
  Map<Class<? extends IReportingEventSource>, Class<? extends Serializable>> getAvailableReporting();
}
