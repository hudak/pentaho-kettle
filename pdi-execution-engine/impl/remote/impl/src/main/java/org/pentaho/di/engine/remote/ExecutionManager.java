package org.pentaho.di.engine.remote;

import com.google.common.base.Preconditions;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.remote.IExecutionManager;
import rx.Observable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Created by hudak on 1/20/17.
 */
public class ExecutionManager implements IExecutionManager {
  @Override public CompletableFuture<?> submit( ITransformation transformation, Map<String, Object> context ) {
    System.out.println( "Received Transformation: " + transformation );
    return CompletableFuture.completedFuture( null );
  }
}
