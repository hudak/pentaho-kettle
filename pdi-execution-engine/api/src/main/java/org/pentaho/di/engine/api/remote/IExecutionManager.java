package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.ITransformation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by hudak on 1/19/17.
 */
public interface IExecutionManager {
  CompletableFuture<?> submit( ITransformation transformation, Map<String, Object> context );
}
