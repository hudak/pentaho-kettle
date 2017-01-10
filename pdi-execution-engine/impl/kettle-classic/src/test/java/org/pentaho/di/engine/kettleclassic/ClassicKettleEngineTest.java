package org.pentaho.di.engine.kettleclassic;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 1/4/17.
 */
public class ClassicKettleEngineTest {
  @Test
  public void execute() throws Exception {

    KettleEnvironment.init();
    ClassicKettleEngine engine = new ClassicKettleEngine();
    TransMeta meta = new TransMeta( "../kettle-native/src/test/resources/lorem.ktr" );
    ITransformation transformation = ClassicUtils.convert( meta );
    IExecutionResult result = engine.prepare( transformation ).execute().get();
  }

}