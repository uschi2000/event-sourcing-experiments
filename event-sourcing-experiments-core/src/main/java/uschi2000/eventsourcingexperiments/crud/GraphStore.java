/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.crud;

import java.util.Set;

/** A simple storage class for a graph that could readily be implemented on a Cassandra-like data model. */
public interface GraphStore {

    /** Adds the given node to the graph. This method is idempotent for existing nodes. */
    void addNode(int nodeId);

    /** Returns the set of nodes in this graph, i.e., exactly the set of nodes that have been {@link #addNode added}. */
    Set<Integer> getNodes();

    /**
     * Adds a directed edge between the two given nodes. This method is idempotent for existing edges and never
     * fails (even if one of the two nodes do not exist).
     */
    void addEdge(int from, int to);

    /**
     * Returns the set of nodes connected to the given node, i.e., exactly the set of edges for which an edge was
     * {@link #addEdge added}. Returns an empty list if the given node has no outgoing edges or if the given node
     * does not exist.
     */
    Set<Integer> getEdges(int from);
}
