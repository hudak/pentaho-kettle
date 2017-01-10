package org.pentaho.di.engine.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 5/31/16.
 */
public interface IExecutionContext {
  Map<String, Object> getParameters();

  Map<String, Object> getEnvironment();

  ITransformation getTransformation();

  @Deprecated
  String[] getArguments();

  CompletableFuture<IExecutionResult> execute();
}
