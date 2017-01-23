package org.pentaho.di.engine.spi;

import org.pentaho.di.engine.api.IHasConfig;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.model.Operation;
import org.pentaho.di.engine.model.Transformation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Socket-safe version of a transformation
 * Non-serializable config values are ignored
 * Created by hudak on 1/18/17.
 */
public final class DehydratedTransformation implements Serializable {

  private static final long serialVersionUID = -2509834852420842832L;

  public static DehydratedTransformation dehydrate( ITransformation transformation ) {
    sanitizeTransformation( transformation );
    return new DehydratedTransformation();
  }

  private static Transformation sanitizeTransformation( ITransformation transformation ) {
    Transformation sanitized = new Transformation( "remote: " + transformation.getId() );
    sanitized.setConfig( sanitizeConfig( transformation ) );

    HashMap<String, Operation> sanitizedOperations = new HashMap<>();
    transformation.getOperations().forEach( op -> {
      Operation operation = sanitized.createOperation( "remote: " + op.getId() );
      operation.setConfig( sanitizeConfig( op ) );
      sanitizedOperations.put( op.getId(), operation );
    } );

    transformation.getHops().forEach( hop -> sanitized.createHop(
      sanitizedOperations.get( hop.getFrom().getId() ),
      sanitizedOperations.get( hop.getTo().getId() ),
      hop.getType()
    ) );

    return sanitized;
  }

  private static Map<String, Object> sanitizeConfig( IHasConfig hasConfig ) {
    return hasConfig.getConfig().entrySet().stream()
      .filter( entry -> entry.getValue() instanceof Serializable )
      .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
  }
}
