/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.graphs;

import com.google.common.graph.Graph;
import com.google.common.graph.Traverser;

public final class MoreGraphs {

    private MoreGraphs() {}

    /** Returns true iff nodes from and to are connected int he given graph. */
    public static <V> boolean connected(Graph<V> graph, V from, V to) {
        Traverser<V> traverser = Traverser.forGraph(graph);
        for (V seen : traverser.breadthFirst(from)) {
            if (to.equals(seen)) {
                return true;
            }
        }

        return false;
    }
}
