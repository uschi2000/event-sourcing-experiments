/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

/**
 * Represents a dense, ordered, append-only sequence of {@link GraphEvent graph mutation events}. {@link #subscribe
 * Subscribers} get notified of all events added to the store. The event sequence is <i>dense</i> in the sense that
 * {@link GraphEvent#seqId event sequence numbers} are exactly the integer sequence <code>0, 1, 2, 3, ...</code>.
 * <p>
 * This interface is typically implemented with a database like MySQL or a persistent stream like Kafka.
 */
public interface EventStore {
    /**
     * Adds the given event to the store and returns true iff the event's {@link GraphEvent#seqId() sequence number}
     * is "the next in the sequence", i.e., one plus the sequence number of the last successfully added event. Note that
     * in the case of concurrent access the "next sequence number" may "jump forward" from the perspective of a single
     * consumer.
     */
    boolean put(GraphEvent event);

    /**
     * Subscribes the given observer to all events with sequence number greater than (not including) the given
     * latestKnownSeqId; the observer will eventually receive all events added to this store.
     */
    void subscribe(GraphEvent.Visitor observer, int latestKnownSeqId);
}
