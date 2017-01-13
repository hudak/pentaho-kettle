package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.reporting.ReportingEvent;
import org.pentaho.di.engine.reporting.ReportingManager;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private final Map<String, Object> parameters = new HashMap<>();
  private final Map<String, Object> environment = new HashMap<>();
  private final ClassicKettleEngine engine;
  private final ITransformation transformation;
  private final ReportingManager reportingManager = new ReportingManager();
  private ExecutorService executorService = Executors.newCachedThreadPool();
  private TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
  private IMetaStore metaStore;
  private Repository repository;

  public ClassicKettleExecutionContext( ClassicKettleEngine engine, ITransformation transformation ) {
    this.engine = engine;
    this.transformation = transformation;
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
    // Collect metrics (this should be done before bindReporting)
    CompletableFuture<Map<IOperation, Metrics>> report = getMetricsReport();

    // Prepare trans for execution
    CompletableFuture.supplyAsync( () -> engine.prepare( this ), executorService )
      // Bind reporting sources
      .thenApply( this::bindReporting )
      // Execute trans
      .thenAcceptAsync( engine::execute, executorService )
      // Signal complete to listeners
      .whenComplete( ( done, exception ) -> {
        if ( exception != null ) {
          report.completeExceptionally( exception );
        }
        reportingManager.completeAll();
      } );

    return report.thenApply( events -> (IExecutionResult) () -> events );
  }

  private CompletableFuture<Map<IOperation, Metrics>> getMetricsReport() {
    CompletableFuture<Map<IOperation, Metrics>> report = new CompletableFuture<>();
    // For each operation
    Observable.from( transformation.getOperations() )
      // Get it's last Metrics snapshot (or empty if nothing is returned)
      .flatMap( source -> reportingManager.getObservable( source, Metrics.class )
        .lastOrDefault( new ReportingEvent<>( source, Metrics::empty ) )
      )
      // Collect results
      .toMap( IReportingEvent::getSource, IReportingEvent::getData )
      .subscribe( report::complete, report::completeExceptionally );
    return report;
  }

  private Trans bindReporting( Trans trans ) {
    Scheduler scheduler = Schedulers.from( executorService );

    // Publish transformation status
    Observable<Status> transStatus = Observable.create( TransMonitor.onSubscribe( trans ) );
    reportingManager.registerEventSource( transformation, Status.class, transStatus );

    // Register metrics for each operation
    for ( IOperation op : transformation.getOperations() ) {
      List<StepInterface> steps = trans.findStepInterfaces( op.getId() );
      // Periodically...
      Observable<Metrics> metrics = Observable.interval( 1, TimeUnit.SECONDS, scheduler )
        // while the transformation is running
        .takeWhile( i -> !trans.isFinished() )
        // take a metrics snapshot
        .flatMap( i -> Observable.from( steps ).map( this::getMetrics ).reduce( Metrics.empty(), Metrics::add ) );

      reportingManager.registerEventSource( op, Metrics.class, metrics );
    }
    return trans;
  }

  private Metrics getMetrics( StepInterface step ) {
    return new Metrics( step.getLinesInput(), step.getLinesOutput(), step.getLinesRejected(), 0 );
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

  @Override public Collection<IReportingEventSource> getReportingSources() {
    return ImmutableList.<IReportingEventSource>builder()
      .add( transformation )
      .addAll( transformation.getOperations() )
      .addAll( transformation.getHops() )
      .build();
  }

  @Override
  public <S extends IReportingEventSource, D extends Serializable>
  Publisher<IReportingEvent<S, D>> eventStream( S source, Class<D> type ) {
    return RxReactiveStreams.toPublisher( reportingManager.getObservable( source, type ) );
  }
}
