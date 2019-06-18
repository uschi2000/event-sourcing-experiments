/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.palantir.eventsourcingexperiments.api.GraphDb;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractGraphDbTest {

    // TODO(rfink): Lot of missing test coverage here... let's ignore it for the sake of the example.

    protected abstract GraphDb createGraphDb();
    protected abstract Graph<Integer> getGraph();

    private GraphDb graph;

    @Before
    public void beforeAbstractGraphDbTest() {
        graph = createGraphDb();
    }

    @Test
    public void canAddNodes() {
        assertThat(graph.addNode(1)).isTrue();
        assertThat(graph.addNode(2)).isTrue();
        assertThat(graph.addNode(3)).isTrue();
    }

    @Test
    public void cannotAddRedundantNodes() {
        assertThat(graph.addNode(1)).isTrue();
        assertThat(graph.addNode(1)).isFalse();
        assertThat(graph.addNode(2)).isTrue();
    }

    @Test
    public void canAddEdges() {
        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);

        assertThat(graph.addEdgeAcyclic(1, 2)).isTrue();
        assertThat(graph.addEdgeAcyclic(2, 3)).isTrue();
    }

    @Test
    public void cannotAddEdgesForNonExistingNodes() {
        assertThat(graph.addEdgeAcyclic(1, 2)).isFalse();
    }

    @Test
    public void cannotAddRedundantEdges() {
        graph.addNode(1);
        graph.addNode(2);
        assertThat(graph.addEdgeAcyclic(1, 2)).isTrue();
        assertThat(graph.addEdgeAcyclic(1, 2)).isFalse();
    }

    @Test
    public void cannotAddEdgesThatYieldCycles() {
        graph.addNode(1);
        graph.addNode(2);
        assertThat(graph.addEdgeAcyclic(1, 2)).isTrue();

        graph.addNode(3);
        assertThat(graph.addEdgeAcyclic(1, 3)).isTrue();

        graph.addNode(4);
        assertThat(graph.addEdgeAcyclic(2, 4)).isTrue();

        assertThat(graph.addEdgeAcyclic(1, 4)).isFalse();
        assertThat(graph.addEdgeAcyclic(2, 3)).isFalse();
        assertThat(graph.addEdgeAcyclic(3, 4)).isFalse();
    }

    @Test
    public void edgesAreNotDirected() {
        graph.addNode(1);
        graph.addNode(2);

        assertThat(graph.addEdgeAcyclic(1, 2)).isTrue();
        assertThat(graph.addEdgeAcyclic(2, 1)).isFalse();
    }

    @Test
    public void detectsConnectedNodes() {
        graph.addNode(1);
        graph.addNode(2);
        assertThat(graph.connected(1, 1)).isTrue();
        assertThat(graph.connected(1, 2)).isFalse();

        graph.addEdgeAcyclic(1, 2);
        assertThat(graph.connected(1, 2)).isTrue();
        assertThat(graph.connected(2, 1)).isTrue();
    }

    @Test(timeout = 10_000)
    public void addNodeIsThreadSafe() throws Exception {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
        int numNodes = 200;
        List<ListenableFuture<?>> futures = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; ++i) {
            int nodeId = i;
            futures.add(executor.submit(() -> graph.addNode(nodeId)));
        }
        Futures.allAsList(futures).get();
        assertThat(getGraph().nodes()).hasSize(numNodes);
    }

    @Test(timeout = 10_000)
    public void addEdgeAcyclicIsThreadSafe() throws Exception {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
        int numNodes = 100;
        List<ListenableFuture<?>> futures = new ArrayList<>(numNodes + numNodes * numNodes);
        for (int i = 0; i < numNodes; ++i) {
            int nodeId = i;
            futures.add(executor.submit(() -> graph.addNode(nodeId)));
        }
        Random random = new Random(0);
        for (int i = 0; i < numNodes * numNodes; ++i) {
            int from = random.nextInt(numNodes);
            int to = random.nextInt(numNodes);
            if (from == to) {
                to += 1;
            }
            int theTo = to;
            futures.add(executor.submit(() -> graph.addEdgeAcyclic(from, theTo)));
        }

        Futures.allAsList(futures).get();
        assertThat(Graphs.hasCycle(getGraph())).isFalse();
    }
}
