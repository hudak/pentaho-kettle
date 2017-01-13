package org.pentaho.di.engine.reporting;

import com.google.common.collect.Maps;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by hudak on 1/11/17.
 */
public class ReportingManager {
  private ConcurrentMap<Topic, Subject<Serializable, Serializable>> subjects = Maps.newConcurrentMap();

  public <S extends IReportingEventSource, D extends Serializable>
  Observable<IReportingEvent<S, D>> getObservable( S source, Class<D> type ) {
    return subjects.computeIfAbsent( new Topic( source, type ), topic -> BehaviorSubject.create() )
      .ofType( type )
      .map( data -> new ReportingEvent<>( source, () -> data ) );
  }

  public <S extends IReportingEventSource, D extends Serializable>
  void registerEventSource( S source, Class<D> type, Observable<D> events ) {
    // If subject has been created, subscribe to events
    // No need to create a subject and subscribe to events if no listeners were created
    Optional.ofNullable( subjects.get( new Topic( source, type ) ) ).ifPresent( events::subscribe );
  }

  public void completeAll() {
    subjects.values().forEach( Observer::onCompleted );
  }

  private static class Topic {
    final IReportingEventSource source;
    final Class<? extends Serializable> type;
    private final String sourceId;
    private final Class<? extends IReportingEventSource> sourceType;

    Topic( IReportingEventSource source, Class<? extends Serializable> type ) {
      this.source = source;
      this.type = type;
      sourceId = source.getId();
      sourceType = source.getClass();
    }

    @Override public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( !( o instanceof Topic ) ) {
        return false;
      }
      Topic topic = (Topic) o;
      return Objects.equals( type, topic.type )
        && Objects.equals( sourceId, topic.sourceId )
        && Objects.equals( sourceType, topic.sourceType );
    }

    @Override public int hashCode() {
      return Objects.hash( type, sourceId, sourceType );
    }
  }
}
