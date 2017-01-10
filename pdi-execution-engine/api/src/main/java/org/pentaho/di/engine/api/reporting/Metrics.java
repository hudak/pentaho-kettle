package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IPDIEvent;

import java.io.Serializable;

/**
 * Created by hudak on 1/5/17.
 */
public class Metrics implements Serializable {
  private final IOperation operation;
  private final long in, out, dropped, inFlight;

  public static Metrics empty( IOperation operation ) {
    return new Metrics( operation, 0, 0, 0, 0 );
  }

  public Metrics( IOperation operation, long in, long out, long dropped, long inFlight ) {
    this.operation = operation;
    this.in = in;
    this.out = out;
    this.dropped = dropped;
    this.inFlight = inFlight;
  }

  /**
   * Get number of {@link IPDIEvent}s into this component
   *
   * @return
   */
  public long getIn() {
    return in;
  }

  /**
   * Get number of {@link IPDIEvent}s out from this component
   *
   * @return
   */
  public long getOut() {
    return out;
  }

  /**
   * Get number of {@link IPDIEvent}s dropped (errorred)
   *
   * @return
   */
  public long getDropped() {
    return dropped;
  }

  /**
   * Get number of {@link IPDIEvent}s currently in-flight
   *
   * @return
   */
  public long getInFlight() {
    return inFlight;
  }

  @Override public String toString() {
    return operation.getId() + "Metrics{" +
      "in=" + in +
      ", out=" + out +
      ", dropped=" + dropped +
      ", inFlight=" + inFlight +
      '}';
  }
}
