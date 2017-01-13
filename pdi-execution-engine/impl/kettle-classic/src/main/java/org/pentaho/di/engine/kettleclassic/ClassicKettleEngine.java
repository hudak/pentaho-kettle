package org.pentaho.di.engine.kettleclassic;

import com.google.common.base.Preconditions;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.engine.api.IEngine;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 1/4/17.
 */
public class ClassicKettleEngine implements IEngine {

  @Override public IExecutionContext prepare( ITransformation trans ) {
    return new ClassicKettleExecutionContext( this, trans );
  }

  private TransMeta getTransMeta( ClassicKettleExecutionContext context ) {
    Preconditions.checkArgument( context.getTransformation() instanceof ClassicTransformation );
    ClassicTransformation transformation = (ClassicTransformation) context.getTransformation();
    return transformation.getTransMeta();
  }

  Trans prepare( ClassicKettleExecutionContext context ) {
    TransMeta transMeta = getTransMeta( context );
    TransExecutionConfiguration executionConfiguration = context.getExecutionConfiguration();

    Trans trans;
    try {
      // Set the requested logging level..
      //
      DefaultLogLevel.setLogLevel( executionConfiguration.getLogLevel() );

      transMeta.injectVariables( executionConfiguration.getVariables() );

      // Set the named parameters
      Map<String, String> paramMap = executionConfiguration.getParams();
      Set<String> keys = paramMap.keySet();
      for ( String key : keys ) {
        transMeta.setParameterValue( key, Const.NVL( paramMap.get( key ), "" ) );
      }

      transMeta.activateParameters();

      // Important: even though transMeta is passed to the Trans constructor, it is not the same object as is in
      // memory
      // To be able to completely test this, we need to run it as we would normally do in pan
      //
      trans = new Trans( transMeta, context.getRepository(), transMeta.getName(),
        transMeta.getRepositoryDirectory().getPath(),
        transMeta.getFilename() );

      trans.setRepository( context.getRepository() );
      trans.setMetaStore( context.getMetaStore() );

      String spoonLogObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject spoonLoggingObject = new SimpleLoggingObject( "SPOON", LoggingObjectType.SPOON, null );
      spoonLoggingObject.setContainerObjectId( spoonLogObjectId );
      spoonLoggingObject.setLogLevel( executionConfiguration.getLogLevel() );
      trans.setParent( spoonLoggingObject );

      trans.setLogLevel( executionConfiguration.getLogLevel() );
      trans.setReplayDate( executionConfiguration.getReplayDate() );
      trans.setRepository( executionConfiguration.getRepository() );
      trans.setMonitored( true );
      trans.setSafeModeEnabled( executionConfiguration.isSafeModeEnabled() );
      trans.setGatheringMetrics( executionConfiguration.isGatheringMetrics() );

      // Launch the step preparation in a different thread.
      // That way Spoon doesn't block anymore and that way we can follow the progress of the initialization
      //

      trans.prepareExecution( context.getArguments() );
      //      log.logMinimal( BaseMessages.getString( PKG, "TransLog.Log.StartedExecutionOfTransformation" ) );
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }

    return trans;
  }

  void execute( Trans trans ) {
    try {
      trans.startThreads();
      trans.waitUntilFinished();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }
}
