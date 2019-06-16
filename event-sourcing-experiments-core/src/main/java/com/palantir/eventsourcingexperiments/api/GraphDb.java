/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.api;

public interface GraphDb {
    /** Adds a node with a given ID to the graph, returns true iff such a node does not exist yet. */
    boolean addNode(int nodeId);

    /**
     * Adds an undirected edge between the two given nodes, returns true iff (a) the two nodes exist, (b) the edge
     * does not exist yet, and (c) the resulting graph is acyclic.
     */
    boolean addEdgeAcyclic(int from, int to);

    /** Returns true iff the two given nodes exist and the exists a path between the nodes in the graph. */
    boolean connected(int from, int to);
}
