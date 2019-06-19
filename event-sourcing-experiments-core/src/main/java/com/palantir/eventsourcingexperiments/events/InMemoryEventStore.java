/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class InMemoryEventStore implements EventStore {

    @GuardedBy("subscribersLock")
    private final List<GraphEvent.Visitor> subscribers = new ArrayList<>();
    private final List<GraphEvent> events = new CopyOnWriteArrayList<>();

    private final AtomicInteger nextSeqId = new AtomicInteger(0);

    // A simple way to guarantee exactly-once delivery is to block delivery of new events until all subscribers
    // have caught up to the "tail" of the event queue. We do this here with a read-write-lock: while a new subscriber
    // is catching up with old events, it must hold the exclusive write lock and thus the put() method
    // cannot deliver any events until the new subscriber has caught up.
    private final ReadWriteLock subscribersLock = new ReentrantReadWriteLock();

    @Override
    public boolean put(GraphEvent event) {
        boolean seqIdIsValid = nextSeqId.compareAndSet(event.seqId(), event.seqId() + 1);
        if (!seqIdIsValid) {
            return false;
        }

        Lock reading = subscribersLock.readLock();
        reading.lock();
        try {
            // order between these two calls does not matter
            events.add(event);
            subscribers.forEach(event::accept);
            return true;
        } finally {
            reading.unlock();
        }
    }

    @Override
    public void subscribe(GraphEvent.Visitor observer, int latestKnownSeqId) {
        Lock writing = subscribersLock.writeLock();
        writing.lock();
        try {
            subscribers.add(observer);
            // It's safe to iterate here because nobody can mutate the events queue because we're holding the write lock
            events.iterator().forEachRemaining(event -> {
                if (event.seqId() > latestKnownSeqId) {
                    event.accept(observer);
                }
            });
        } finally {
            writing.unlock();
        }
    }
}
