/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

import com.google.common.graph.Graph;
import com.palantir.eventsourcingexperiments.AbstractGraphDbTest;
import com.palantir.eventsourcingexperiments.api.GraphDb;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class EventGraphDbTest extends AbstractGraphDbTest {

    private MemoryImage graph;

    @Override
    protected GraphDb createGraphDb() {
        graph = new MemoryImage();
        return new EventGraphDb(new InMemoryEventStore(), graph);
    }

    @Override
    protected Graph<Integer> getGraph() {
        return graph.graph().graph();
    }
}
