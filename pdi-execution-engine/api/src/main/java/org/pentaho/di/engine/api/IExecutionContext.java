package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.IProgressReporting;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 5/31/16.
 */
public interface IExecutionContext extends IProgressReporting {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  ITransformation getTransformation();

  @Deprecated
  String[] getArguments();

  CompletableFuture<IExecutionResult> execute();
}
