package org.pentaho.di.core.jdbc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class ThinDriverTest {
  private static final String URL = "jdbc:pdi://localhost:9080/kettle";
  ThinDriver driver;

  Properties properties;

  @Before
  public void setUp() throws Exception {
    driver = new ThinDriver();

    properties = new Properties();
    properties.setProperty( "user", "joe" );
    properties.setProperty( "password", "drowssap" );
  }

  @Test
  public void testDelegateUnavailable() throws Exception {
    // Driver accepts the URL but returns null on connect, logs a warning
    assertFalse( driver.acceptsURL( URL ) );
    assertNull( "Unexpected connection object", driver.connect( URL, properties ) );
  }

  @Test
  public void testInvalidUrl() throws SQLException {
    final String otherURL = "jdbc:someOther://localhost:1234/db";
    assertFalse( driver.acceptsURL( otherURL ) );
    assertNull( "Unexpected connection object returned", driver.connect( otherURL, properties ) );
  }
}
