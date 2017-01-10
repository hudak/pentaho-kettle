package org.pentaho.di.engine.api.reporting;

/**
 * Created by hudak on 1/6/17.
 */
public enum Status {
  STOPPED( false ),
  RUNNING( false ),
  PAUSED( false ),
  FAILED( true ),
  FINISHED( true );

  final boolean finalState;

  Status( Boolean finalState ) {
    this.finalState = finalState;
  }

  public boolean isFinal() {
    return finalState;
  }
}
