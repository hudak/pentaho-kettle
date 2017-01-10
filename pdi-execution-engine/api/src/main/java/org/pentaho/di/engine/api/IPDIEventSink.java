package org.pentaho.di.engine.api;

import java.util.function.Consumer;

/**
 * Created by nbaker on 1/7/17.
 */
public interface IPDIEventSink<T extends IPDIEvent> extends Consumer<IPDIEventSource<T>> {
}
