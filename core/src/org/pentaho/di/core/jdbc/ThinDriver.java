/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @deprecated As of release 6.0, use org.pentaho.di.trans.dataservice.jdbc.ThinDriver instead
 *
 * Data Service client code is now available in the pdi-dataservice-plugin project
 *
 */
@Deprecated
public class ThinDriver implements Driver {

  public static final String BASE_URL = "jdbc:pdi://";
  public static final String SERVICE_NAME = "/kettle";
  public static final String ALT_DRIVER = "org.pentaho.di.trans.dataservice.jdbc.ThinDriver";
  private static final Logger logger = Logger.getLogger( ThinDriver.class.getName() );

  static {
    warnDeprecated();
    try {
      // Try to activate new Driver
      Class.forName( ALT_DRIVER );
    } catch ( ClassNotFoundException e ) {
      logger.warning( ALT_DRIVER + " is not installed. Please add pdi-dataservice-client to your classpath." );
    }
  }

  public ThinDriver() {
    warnDeprecated();
  }

  @Override
  public boolean acceptsURL( String url ) {
    return false;
  }

  @Override
  public Connection connect( String url, Properties properties ) {
    warnDeprecated();
    return null;
  }

  @Override
  public int getMajorVersion() {
    return 0;
  }

  @Override
  public int getMinorVersion() {
    return 1;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( String arg0, Properties arg1 ) throws SQLException {
    return null;
  }

  @Override
  public boolean jdbcCompliant() {
    return false;
  }

  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return logger;
  }

  private static void warnDeprecated() {
    logger.warning( ThinDriver.class + " has been deprecated. Please use " + ALT_DRIVER + " instead." );
  }

}
