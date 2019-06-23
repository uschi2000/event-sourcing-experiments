/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.graphs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class MoreGraphsTest {

    @Test
    public void checkReachableSanity() {
        MutableGraph<Integer> graph = GraphBuilder.undirected().build();
        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);
       
        assertThat(MoreGraphs.reachable(graph, 1)).contains(1);
        assertThat(MoreGraphs.reachable(graph, 2)).contains(2);
        assertThat(MoreGraphs.reachable(graph, 3)).contains(3);

        graph.putEdge(1, 2);
        assertThat(MoreGraphs.reachable(graph, 1)).contains(1, 2);
        assertThat(MoreGraphs.reachable(graph, 2)).contains(1, 2);
        assertThat(MoreGraphs.reachable(graph, 3)).contains(3);

        graph.putEdge(2, 3);
        assertThat(MoreGraphs.reachable(graph, 1)).contains(1, 2, 3);
        assertThat(MoreGraphs.reachable(graph, 2)).contains(1, 2, 3);
        assertThat(MoreGraphs.reachable(graph, 3)).contains(1, 2, 3);
    }
}
