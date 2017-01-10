package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.metastore.api.IMetaStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private final Map<String, Object> parameters = new HashMap<>();
  private final Map<String, Object> environment = new HashMap<>();
  private final ClassicKettleEngine engine;
  private final ITransformation transformation;
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
  private IMetaStore metaStore;
  private Repository repository;

  public ClassicKettleExecutionContext( ClassicKettleEngine engine, ITransformation trans ) {
    this.engine = engine;
    this.transformation = trans;
  }

  @Override public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override public Map<String, Object> getEnvironment() {
    return environment;
  }

  @Override public ITransformation getTransformation() {
    return transformation;
  }

  public void setExecutionConfiguration( TransExecutionConfiguration executionConfiguration ) {
    this.executionConfiguration = executionConfiguration;
  }

  public TransExecutionConfiguration getExecutionConfiguration() {
    return executionConfiguration;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  @Override public String[] getArguments() {
    return new String[ 0 ];
  }

  @Override public CompletableFuture<IExecutionResult> execute() {
    ClassicKettleExecutionContext context = this;
    CompletableFuture<Trans> trans = CompletableFuture.supplyAsync( () -> engine.execute( context ), executorService );
    CompletableFuture<Void> finished = trans.thenAcceptAsync( Trans::waitUntilFinished, executorService );
    return trans.thenCombineAsync( finished, ( t, f ) -> new ClassicExecutionResult( context, t ) );
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public Repository getRepository() {
    return repository;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setExecutorService( ExecutorService executorService ) {
    this.executorService = executorService;
  }
}
