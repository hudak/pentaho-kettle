package org.pentaho.di.engine.api;

import java.io.Serializable;

/**
 * Created by hudak on 1/5/17.
 */
public class Metrics implements Serializable {
  private static final Metrics EMPTY = new Metrics( 0, 0, 0, 0 );

  private final long in, out, dropped, inFlight;

  public static Metrics empty() {
    return EMPTY;
  }

  public Metrics( long in, long out, long dropped, long inFlight ) {
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
  public long getOut(){
    return out;
  }

  /**
   * Get number of {@link IPDIEvent}s dropped (errorred)
   *
   * @return
   */
  public long getDropped(){
    return dropped;
  }

  /**
   * Get number of {@link IPDIEvent}s currently in-flight
   *
   * @return
   */
  public long getInFlight(){
    return inFlight;
  }

  @Override public String toString() {
    return "Metrics{" +
      "in=" + in +
      ", out=" + out +
      ", dropped=" + dropped +
      ", inFlight=" + inFlight +
      '}';
  }
}
