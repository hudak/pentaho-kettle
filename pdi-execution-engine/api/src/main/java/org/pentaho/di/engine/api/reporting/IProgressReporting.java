package org.pentaho.di.engine.api.reporting;

import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.List;

/**
 * Implementations report on their status along with metrics associated with execution progress.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public interface IProgressReporting {
  /**
   * @return list of possible reporting topics published by this component
   */
  List<String> getTopics();

  /**
   * Returns a reactive stream of reporting events.
   * The topics of these events should be included in {@link * #getTopics()}
   *
   * @return Publisher of events from this component
   */
  Publisher<? extends IProgressReporting.Event> getEvents();

  interface Event<T> extends Serializable {
    String getTopic();

    T getData();
  }
}
