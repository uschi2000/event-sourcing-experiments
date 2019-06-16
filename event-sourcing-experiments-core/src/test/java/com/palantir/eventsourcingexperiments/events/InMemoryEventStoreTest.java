/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class InMemoryEventStoreTest {

    private static final GraphEvent.NodeAdded E1 = GraphEvent.nodeAdded(1);
    private static final GraphEvent.EdgeAdded E2 = GraphEvent.edgeAdded(1, 2);
    private static final GraphEvent.EdgeAdded E3 = GraphEvent.edgeAdded(1, 3);

    private EventStore store;
    @Mock private GraphEvent.Visitor observer1;
    @Mock private GraphEvent.Visitor observer2;

    @Before
    public void before() {
        store = new InMemoryEventStore();
    }

    @Test
    public void canAddEvents() {
        store.put(E1);
        store.put(E2);
    }

    @Test
    public void existingSubscribersReceiveNewEvents() {
        store.subscribe(observer1);
        store.subscribe(observer2);

        store.put(E1);
        store.put(E2);

        verify(observer1).visit(E1);
        verify(observer2).visit(E1);
        verify(observer1).visit(E2);
        verify(observer2).visit(E2);
    }

    @Test
    public void newSubscribersReceiveOldEventsAndThenListenLive() {
        store.put(E1);
        store.put(E2);
        store.subscribe(observer1);
        verify(observer1).visit(E1);
        verify(observer1).visit(E2);
        verifyNoMoreInteractions(observer1);

        store.put(E3);
        verify(observer1).visit(E3);
    }
}