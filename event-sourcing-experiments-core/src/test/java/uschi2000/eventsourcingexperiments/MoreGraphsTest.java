/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uschi2000.eventsourcingexperiments.graphs.MoreGraphs;

@RunWith(MockitoJUnitRunner.class)
public final class MoreGraphsTest {

    @Test
    public void checkSanity() {
        MutableGraph<Integer> graph = GraphBuilder.undirected().build();
        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);
        assertThat(MoreGraphs.connected(graph, 1, 2)).isFalse();
        assertThat(MoreGraphs.connected(graph, 2, 3)).isFalse();
        assertThat(MoreGraphs.connected(graph, 1, 3)).isFalse();

        graph.putEdge(1, 2);
        assertThat(MoreGraphs.connected(graph, 1, 2)).isTrue();
        assertThat(MoreGraphs.connected(graph, 2, 3)).isFalse();
        assertThat(MoreGraphs.connected(graph, 1, 3)).isFalse();

        graph.putEdge(2, 3);
        assertThat(MoreGraphs.connected(graph, 1, 2)).isTrue();
        assertThat(MoreGraphs.connected(graph, 2, 3)).isTrue();
        assertThat(MoreGraphs.connected(graph, 1, 3)).isTrue();
    }
}
