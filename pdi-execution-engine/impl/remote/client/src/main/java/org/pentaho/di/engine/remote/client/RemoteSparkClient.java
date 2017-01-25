package org.pentaho.di.engine.remote.client;

import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;

/**
 * Created by hudak on 1/25/17.
 */
public class RemoteSparkClient implements IEngine {

  @Override public IExecutionContext prepare( ITransformation trans ) {
    return new Context( this );
  }
}
