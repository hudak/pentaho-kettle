package org.pentaho.di.engine.remote;

import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.remote.IExecutionManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by hudak on 1/20/17.
 */
public class ExecutionManager implements IExecutionManager {
  @Override public CompletableFuture<?> submit( ITransformation transformation, Map<String, Object> context ) {
    System.out.println( "Received Transformation: " + transformation );
    return CompletableFuture.completedFuture( null );
  }
}
