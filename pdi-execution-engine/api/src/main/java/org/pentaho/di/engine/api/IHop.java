package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.reporting.IProgressReporting;

/**
 * @author nhudak
 */
public interface IHop extends IProgressReporting {

  String TYPE_NORMAL = "NORMAL";

  IOperation getFrom();

  IOperation getTo();

  default String getType() {
    return TYPE_NORMAL;
  }

}
