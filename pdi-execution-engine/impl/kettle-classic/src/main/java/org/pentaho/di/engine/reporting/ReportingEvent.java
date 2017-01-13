package org.pentaho.di.engine.reporting;

import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Created by hudak on 1/12/17.
 */
public class ReportingEvent<S extends IReportingEventSource, D extends Serializable>
  implements IReportingEvent<S, D> {
  private final S source;
  private final Supplier<D> data;

  public ReportingEvent( S source, Supplier<D> data ) {
    this.source = source;
    this.data = data;
  }

  @Override public S getSource() {
    return source;
  }

  @Override public D getData() {
    return data.get();
  }
}
