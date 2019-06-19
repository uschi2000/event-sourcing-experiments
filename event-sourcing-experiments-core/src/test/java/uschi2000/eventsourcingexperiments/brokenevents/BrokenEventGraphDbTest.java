/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.brokenevents;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.graph.Graphs;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uschi2000.eventsourcingexperiments.GraphDb;
import uschi2000.eventsourcingexperiments.events.InMemoryEventStore;

@RunWith(MockitoJUnitRunner.class)
public final class BrokenEventGraphDbTest {

    private GraphDb graph;

    @Before
    public void before() {
        graph = new BrokenEventGraphDb(new InMemoryEventStore());
    }

    @Ignore  // this can fail (non-deterministically) because the implementation is not actually thread-safe
    @Test(timeout = 20_000)
    public void addEdgeAcyclicIsThreadSafe() throws Exception {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
        int numNodes = 300;
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
        assertThat(Graphs.hasCycle(graph.getGraph())).isFalse();
    }
}
