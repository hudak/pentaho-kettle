package org.pentaho.di.engine.api.remote;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * A service (usually running on a remote daemon) that can accept Execution Requests
 * <p>
 * Created by hudak on 2/7/17.
 */
public interface ExecutionManager {
  /**
   * @return common name this service's backing engine, e.g. "Spark"
   */
  String getEngineType();

  /**
   * @return Name for this cluster, can be shared by other daemons. Assume "default" if unknown
   */
  String getClusterName();

  /**
   * @return environment of backing engine
   */
  Map<String, Serializable> getEnvironment();

  /**
   * Submit a transformation to this execution manager.
   * <p>
   * If submission was successful, a server socket address will be returned, which can then be used to
   * stream events
   *
   * @param request {@link ExecutionRequest}
   * @return URI with the format tcp://$host:$port
   * @throws IOException Request could not be submitted event stream server could not start
   */
  URI submit( ExecutionRequest request ) throws IOException;
}
