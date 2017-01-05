package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.engine.api.IExecutableOperation;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IPDIEventSource;
import org.pentaho.di.engine.api.reporting.IProgressReporting;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Metrics;
import org.pentaho.di.trans.TransMeta;
import rx.Observable;
import rx.RxReactiveStreams;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.assertThat;

public class EngineITest {

  private TransMeta testMeta;
  static Engine engine = new Engine();

  @Before
  public void before() throws KettleException {
    KettleEnvironment.init();
    testMeta = new TransMeta( getClass().getClassLoader().getResource( "test2.ktr" ).getFile() );
  }

  @Test
  public void testExec() throws KettleXMLException, KettleMissingPluginsException, InterruptedException {
    TransMeta meta = new TransMeta( getClass().getClassLoader().getResource( "lorem.ktr" ).getFile() );
    ITransformation trans = Transformation.convert( meta );
    engine.execute( trans );
  }

  @Test
  public void test2Sources1Sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "2InputsWithConsistentColumns.ktr" );
    Map<String, Metrics> reports = getDataEventReport( result );
    System.out.println( reports );
    assertThat( reports, aMapWithSize( 3 ) );
    Metrics dataGrid1 = getByName( "Data Grid", reports );
    Metrics dataGrid2 = getByName( "Data Grid 2", reports );
    Metrics dummy = getByName( "Dummy (do nothing)", reports );
    System.out.println( reports );
    assertThat( dataGrid1.getOut(), is( 1l ) );
    assertThat( dataGrid2.getOut(), is( 1l ) );
    assertThat( "dummy should get rows fromm both data grids", dummy.getIn(), is( 2l ) );
  }

  private static Map<String, Metrics> getDataEventReport( IExecutionResult result ) {
    List<IOperation> operations = result.getTransformation().getOperations();
    Map<String, IOperation> topics = operations.stream()
      .collect( Collectors.toMap( op -> KettleExecOperation.topic( op, "metrics" ), Function.identity() ) );

    return RxReactiveStreams.toObservable( result.getEvents() )
      .groupBy( IProgressReporting.Event::getTopic, IProgressReporting.Event::getData )
      .flatMap( topic -> {
          Preconditions.checkState( topics.containsKey( topic.getKey() ) );
          String id = topics.get( topic.getKey() ).getId();
          return topic
            .ofType( Metrics.class )
            .lastOrDefault( Metrics.empty() )
            .map( value -> Maps.immutableEntry( id, value ) );
        }
      )
      .toMap( Map.Entry::getKey, Map.Entry::getValue )
      .toBlocking()
      .single();
  }

  @Test
  public void test1source2trans1sink()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "1source.2Trans.1sink.ktr" );
    Map<String, Metrics> reports = getDataEventReport( result );
    System.out.println( reports );
    assertThat( reports, aMapWithSize( 5 ) );
  }

  @Test @Ignore // Missing KTR?
  public void simpleFilter()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "simpleFilter.ktr" );
    Map<String, Metrics> reports = getDataEventReport( result );
    System.out.println( reports );

  }

  @Test
  public void testLookup()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    IExecutionResult result = getTestExecutionResult( "SparkSample.ktr" );
    Map<String, Metrics> reports = getDataEventReport( result );
    Thread.sleep( 100 );  // Don't check before file is done being written
    assertThat( getByName( "Merged Output", reports ).getOut(), is( 2001l ) );  // hmm, out + written
    System.out.println( reports );
  }

  private Stream<IExecutableOperation> asExecutableOperation( IProgressReporting report ) {
    return report instanceof IExecutableOperation ? Stream.of( ( (IExecutableOperation) report ) ) : Stream.empty();
  }

  @Test
  public void testChainedCalc()
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException, ExecutionException {
    // executes a series of Calculation steps as a chain of Spark FlatMapFunctions.
    IExecutionResult result = getTestExecutionResult( "StringCalc.ktr" );
    Map<String, Metrics> reports = getDataEventReport( result );
    System.out.println( reports );

  }


  private Predicate<? super IPDIEventSource> isOp( String s ) {
    return o -> o.getId().equals( s );
  }


  private IExecutionResult getTestExecutionResult( String transName )
    throws KettleXMLException, KettleMissingPluginsException, InterruptedException,
    ExecutionException {
    String ktr = Optional.of( getClass().getClassLoader() )
      .flatMap( cl -> Optional.ofNullable( cl.getResource( transName ) ) )
      .flatMap( url -> Optional.ofNullable( url.getFile() ) )
      .orElseThrow( () -> new AssertionError( transName + " not found" ) );
    TransMeta meta = new TransMeta( ktr );
    ITransformation trans = Transformation.convert( meta );
    return engine.execute( trans );
  }

  private Metrics getByName( String name, Map<String, Metrics> reports ) {
    return reports.keySet().stream()
      .filter( topic -> topic.contains( name ) )
      .map( reports::get )
      .findFirst()
      .orElseThrow( () -> new AssertionError( "No metrics found for " + name ) );
  }


}