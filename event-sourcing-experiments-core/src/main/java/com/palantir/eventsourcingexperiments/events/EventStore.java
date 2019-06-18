/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

public interface EventStore {
    /**
     * Adds the given event to the store and returns true iff the event's {@link GraphEvent#seqId() sequence number}
     * is "the next in the sequence", i.e., one plus the sequence number of the last successfully added event. Note that
     * in the case of concurrent access the "next sequence number" may "jump forward" from the perspective of a single
     * consumer.
     */
    boolean put(GraphEvent event);

    /** Subscribes the given observer to events; the observer will eventually receive all events added to this store. */
    void subscribe(GraphEvent.Visitor observer);
}
