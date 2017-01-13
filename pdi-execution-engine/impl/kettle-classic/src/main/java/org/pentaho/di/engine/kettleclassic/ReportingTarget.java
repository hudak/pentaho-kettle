package org.pentaho.di.engine.kettleclassic;

import com.google.common.base.Preconditions;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;

import java.io.Serializable;

/**
 * Created by hudak on 1/11/17.
 */
public class ReportingTarget<S extends IReportingEventSource, D extends Serializable> {
  private final BehaviorSubject<IReportingEvent<S, D>> subject = BehaviorSubject.create();
  private final S source;
  private final Class<D> type;

  public ReportingTarget( S source, Class<D> type ) {
    this.source = source;
    this.type = type;
  }

  public S getSource() {
    return source;
  }

  public Class<D> getType() {
    return type;
  }

  public Observable<IReportingEvent<S, D>> asObservable() {
    return subject.asObservable();
  }

  public Observer<D> asObserver() {
    return new Observer<D>() {
      @Override public void onCompleted() {
        subject.onCompleted();
      }

      @Override public void onError( Throwable e ) {
        subject.onError( e );
      }

      @Override public void onNext( D d ) {
        Preconditions.checkArgument( type.isInstance( d ) );
        subject.onNext( new IReportingEvent<S, D>() {
          @Override public S getSource() {
            return source;
          }

          @Override public D getData() {
            return d;
          }
        } );
      }
    };
  }
}
