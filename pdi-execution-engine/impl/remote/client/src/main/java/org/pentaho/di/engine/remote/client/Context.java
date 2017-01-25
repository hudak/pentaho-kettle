package org.pentaho.di.engine.remote.client;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by hudak on 1/25/17.
 */
class Context implements IExecutionContext {
  private final RemoteSparkClient client;

  public Context( RemoteSparkClient client ) {
    this.client = client;
  }

  @Override public Map<String, Object> getParameters() {
    return null;
  }

  @Override public Map<String, Object> getEnvironment() {
    return null;
  }

  @Override public ITransformation getTransformation() {
    return null;
  }

  @Override public String[] getArguments() {
    return new String[ 0 ];
  }

  @Override public CompletableFuture<IExecutionResult> execute() {
    return null;
  }

  @Override
  public <S extends IReportingEventSource, D extends Serializable> Publisher<IReportingEvent<S, D>> eventStream(
    S source, Class<D> type ) {
    return null;
  }

  @Override public Collection<IReportingEventSource> getReportingSources() {
    return null;
  }
}
