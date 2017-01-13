package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicTransformation implements ITransformation {
  private List<ClassicOperation> operations;
  private TransMeta transMeta;

  public ClassicTransformation( TransMeta transMeta ) {

    this.transMeta = transMeta;
  }

  @Override public List<IOperation> getOperations() {
    return null;
  }

  @Override public List<IOperation> getSourceOperations() {
    return null;
  }

  @Override public List<IOperation> getSinkOperations() {
    return null;
  }

  @Override public List<IHop> getHops() {
    return null;
  }

  @Override public String getConfig() {
    return null;
  }

  @Override public String getId() {
    return null;
  }

  public <R> void setOperations( List<ClassicOperation> operations ) {
    this.operations = operations;
    this.operations.forEach( o -> o.setTransformation( ClassicTransformation.this ) );
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }
}
