package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransStoppedListener;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hudak on 1/12/17.
 */
class TransMonitor implements TransListener, TransStoppedListener {
  private final Subscriber<? super Status> subscriber;

  private TransMonitor( Subscriber<? super Status> subscriber ) {
    this.subscriber = subscriber;
  }

  public static Observable.OnSubscribe<Status> onSubscribe( Trans trans ) {
    return ( subscriber ) -> {
      TransMonitor monitor = new TransMonitor( subscriber );
      trans.addTransListener( monitor );
      trans.addTransStoppedListener( monitor );
    };
  }

  @Override public void transStarted( Trans trans ) throws KettleException {
    if ( !subscriber.isUnsubscribed() ) {
      subscriber.onNext( Status.RUNNING );
    }
  }

  @Override public void transActive( Trans trans ) {
    // All threads have started. do we have an event for that?
  }

  @Override public void transFinished( Trans trans ) throws KettleException {
    if ( !subscriber.isUnsubscribed() ) {
      subscriber.onNext( trans.getErrors() == 0 ? Status.FINISHED : Status.FAILED );
      subscriber.onCompleted();
    }
  }

  @Override public void transStopped( Trans trans ) {
    if ( !subscriber.isUnsubscribed() ) {
      subscriber.onNext( Status.STOPPED );
    }
  }
}
