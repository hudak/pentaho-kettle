package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by hudak on 1/13/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class ClassicKettleExecutionContextTest {
  @Mock private ClassicKettleEngine engine;
  @Mock private ITransformation transformation;
  @Mock private IOperation operation;
  private ClassicKettleExecutionContext context;

  @Before
  public void setUp() throws Exception {
    when( transformation.getOperations() ).thenReturn( ImmutableList.of( operation ) );
    when( transformation.getHops() ).thenReturn( ImmutableList.of() );
    context = new ClassicKettleExecutionContext( engine, transformation );
  }

  @Test
  public void canSubscribeToStepMetrics() throws Exception {
    ArrayList<Metrics> snapshots = Lists.newArrayList();
    context.subscribe( operation, Metrics.class, snapshots::add );
  }

  @Test
  public void canSubscribeToAllStepMetrics() throws Exception {
    ArrayList<Metrics> snapshots = Lists.newArrayList();
    context.eventStream( operation, Metrics.class ).subscribe( new Subscriber<IReportingEvent<IOperation, Metrics>>() {
      @Override public void onSubscribe( Subscription s ) {

      }

      @Override public void onNext( IReportingEvent<IOperation, Metrics> iOperationMetricsIReportingEvent ) {
        IOperation source = iOperationMetricsIReportingEvent.getSource();
        Metrics data = iOperationMetricsIReportingEvent.getData();
      }

      @Override public void onError( Throwable t ) {

      }

      @Override public void onComplete() {

      }
    } );
    context.subscribeAll( IOperation.class, Metrics.class, ( source, event ) -> {
      assertThat( source, is( operation ) );
      snapshots.add( event );
    } );
  }
}