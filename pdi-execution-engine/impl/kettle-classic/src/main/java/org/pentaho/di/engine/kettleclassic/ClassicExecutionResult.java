package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.trans.Trans;

import java.util.Map;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicExecutionResult implements IExecutionResult {
  private final ClassicKettleExecutionContext context;
  private final Trans trans;

  public ClassicExecutionResult( ClassicKettleExecutionContext context, Trans trans ) {
    this.context = context;
    this.trans = trans;
  }

  @Override public IExecutionContext getContext() {
    return context;
  }

  @Override public Map<IOperation, Metrics> getDataEventReport() {
    return null;
  }
}
