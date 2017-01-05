package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutableOperationFactory;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.engine.kettlenative.impl.factories.KettleExecOperationFactory;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Engine implements IEngine {

  private final ExecutorService executorService = Executors.newFixedThreadPool( 10 );

  private final JavaSparkContext javaSparkContext = new JavaSparkContext(
    new SparkConf()
      .setAppName( "AEL" )
      .setMaster( "local[2]" ) );

  // Pulling out Spark factory for now
  private final List<IExecutableOperationFactory> factories = ImmutableList.of( new KettleExecOperationFactory() );

  @Override public IExecutionResult execute( ITransformation trans ) {
    initKettle();
    // convert ops to executable ops
    List<IExecutableOperation> execOps = getExecutableOperations( trans,
      getExecContext() );
    // wire up the execution graph
    wireExecution( execOps );
    // submit for execution
    start( trans, execOps );

    return new IExecutionResult() {
      private final ImmutableList<String> topics = ImmutableList.copyOf(
        execOps.stream().flatMap( exOp -> exOp.getTopics().stream() ).iterator()
      );

      @Override public Status getStatus() {
        return null;
      }

      @Override public ITransformation getTransformation() {
        return trans;
      }

      @Override public IEngine getEngine() {
        return Engine.this;
      }

      @Override public List<String> getTopics() {
        return topics;
      }

      @Override public Publisher<Event> getEvents() {
        Observable<Event> allEvents = Observable.from( execOps )
          .map( IExecutableOperation::getEvents )
          .flatMap( RxReactiveStreams::toObservable );
        return RxReactiveStreams.toPublisher( allEvents );
      }
    };
  }

  private List<IExecutableOperation> getExecutableOperations( ITransformation trans, IExecutionContext context ) {
    return trans.getOperations()
      .stream()
      .map( op -> getExecOp( trans, op, context ) )
      .collect( Collectors.toList() );
  }

  private IExecutableOperation getExecOp( ITransformation trans, IOperation op, IExecutionContext context ) {
    return factories.stream()
      .map( factory -> factory.create( trans, op, context ) )
      .filter( o -> o.isPresent() )
      .findFirst()
      .get()
      .orElseThrow( () -> new RuntimeException( "Couldn't create an executable op for " + op.getId() ) );
  }

  private void wireExecution( List<IExecutableOperation> execOps ) {
    // for each operation, subscribe to the set of "from" ops.
    execOps.stream()
      .forEach( op ->
        op.getFrom().stream()
          .map( fromOp -> getExecOp( fromOp, execOps ) )
          .forEach( fromExecOp -> fromExecOp.subscribe( op ) )
      );
  }

  private IExecutableOperation getExecOp( IOperation op, List<IExecutableOperation> execOps ) {
    return execOps.stream()
      .filter( execOp -> execOp.getId().equals( op.getId() ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "no matching exec op" ) );
  }

  private void start( ITransformation trans, List<IExecutableOperation> execOps ) {
    // invoke each source operation
    trans.getSourceOperations().stream()
      .map( op -> getExecOp( op, execOps ) )
      .forEach( execOp -> execOp.onNext( KettleDataEvent.empty() ) );
  }

  /**
   * temp hack for testing.  context should be passed in to the engine
   **/
  private IExecutionContext getExecContext() {
    return new ExecutionContext( Collections.emptyMap(),
      ImmutableMap.of( "sparkcontext", javaSparkContext,
        "executor", executorService ) );
  }

  private void initKettle() {
    try {
      if ( !KettleEnvironment.isInitialized() ) {
        KettleEnvironment.init();
      }
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

}
