/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.graph.Graph;
import java.util.Set;

/** A simple dummy graph database API for maintaining acyclic graphs. */
public interface GraphDb {
    /** Adds a node with a given ID to the graph, returns true iff such a node does not exist yet. */
    boolean addNode(int nodeId);

    /**
     * Adds an undirected edge between the two given nodes, returns true iff (a) the two nodes exist, (b) the edge
     * does not exist yet, and (c) the resulting graph is acyclic.
     */
    boolean addEdgeAcyclic(int from, int to);

    /** Returns the set of nodes reachable from a given seed node. */
    Set<Integer> reachable(int seed);

    // Bit of a hack, just for demonstration/testing purposes
    @VisibleForTesting
    Graph<Integer> getGraph();
}
