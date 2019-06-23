/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.graphs;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.Traverser;
import java.util.Set;

public final class MoreGraphs {

    private MoreGraphs() {}

    /** Returns the set of nodes reachable from a given seed node. */
    public static <V> Set<V> reachable(Graph<V> graph, V seed) {
        return ImmutableSet.copyOf(Traverser.forGraph(graph).breadthFirst(seed));
    }
}
