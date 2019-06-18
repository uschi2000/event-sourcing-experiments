/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.crud;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.palantir.eventsourcingexperiments.AbstractGraphDbTest;
import com.palantir.eventsourcingexperiments.api.GraphDb;
import java.util.Set;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class CrudGraphDbTest extends AbstractGraphDbTest {

    private InMemoryGraphStore store;

    @Override
    protected GraphDb createGraphDb() {
        store = new InMemoryGraphStore();
        return new CrudGraphDb(store, new FakeDistributedLock());
    }

    @Override
    protected Graph<Integer> getGraph() {
        Set<Integer> nodes = store.getNodes();
        MutableGraph<Integer> graph = GraphBuilder.undirected().expectedNodeCount(nodes.size()).build();
        nodes.forEach(graph::addNode);
        nodes.forEach(from -> store.getEdges(from).forEach(to -> graph.putEdge(from, to)));
        return graph;
    }
}
