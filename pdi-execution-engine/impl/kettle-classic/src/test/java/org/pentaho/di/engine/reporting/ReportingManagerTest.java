package org.pentaho.di.engine.reporting;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by hudak on 1/13/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class ReportingManagerTest {
  private static final List<Integer> DATA = ImmutableList.copyOf( IntStream.range( 0, 10 ).iterator() );
  private ReportingManager reportingManager;
  @Mock private IReportingEventSource source;
  @Mock private Action1<IReportingEvent<IReportingEventSource, Integer>> onNext;
  @Mock private Action0 onComplete;
  @Captor private ArgumentCaptor<IReportingEvent<IReportingEventSource, Integer>> events;

  @Before
  public void setUp() throws Exception {
    when( source.getId() ).thenReturn( UUID.randomUUID().toString() );
    reportingManager = new ReportingManager();

    // Subscribe to DATA
    reportingManager.getObservable( source, Integer.class ).subscribe( onNext, Throwables::propagate, onComplete );
  }

  @Test
  public void canSubscribeToEventStream() throws Exception {
    reportingManager.registerEventSource( source, Integer.class, Observable.from( DATA ) );

    // Collect events to allow synchronous testing
    verify( onNext, times( 10 ) ).call( events.capture() );
    verify( onComplete ).call();

    assertThat( events.getAllValues(), everyItem( hasProperty( "source", is( source ) ) ) );

    reportingManager.completeAll();

    verifyNoMoreInteractions( onNext, onComplete );
  }

  @Test
  public void canSubscribeToUnregisteredEventSource() throws Exception {
    reportingManager.completeAll();

    verify( onNext, never() ).call( any() );
    verify( onComplete ).call();
  }
}