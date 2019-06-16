/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

public interface EventStore {
    void put(GraphEvent event);
    void subscribe(GraphEvent.Visitor observer);
}
