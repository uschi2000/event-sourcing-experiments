/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.crud;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import java.util.Set;
import uschi2000.eventsourcingexperiments.GraphDb;
import uschi2000.eventsourcingexperiments.graphs.MoreGraphs;

/**
 * A simple {@link GraphDb} implementation. Since the backing {@link GraphStore} makes no transactional
 * guarantees between different requests we need take care of data races here. The simplest way is to use a lock shared
 * by all actors accessing the store; if multiple instances of this class are deployed in a distributed setting, then
 * the simplest implementation uses a distributed lock shared by all instances.
 */
public final class CrudGraphDb implements GraphDb {

    private final GraphStore store;
    private final DistributedLock lock;

    public CrudGraphDb(GraphStore store, DistributedLock lock) {
        this.store = store;
        this.lock = lock;
    }

    @Override
    public boolean addNode(int nodeId) {
        lock.lock();
        try {
            if (containsNode(nodeId)) {
                return false;
            } else {
                store.addNode(nodeId);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addEdgeAcyclic(int from, int to) {
        lock.lock();
        try {
            if (!(containsNode(from) && containsNode(to))) {
                return false;
            }

            MutableGraph<Integer> graph = readFromStore();
            if (graph.hasEdgeConnecting(from, to)) {
                return false;
            }

            graph.putEdge(from, to);
            if (Graphs.hasCycle(graph)) {
                return false;
            }

            // At this point we have an acyclic graph and it's OK to store the edge.
            store.addEdge(from, to);
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean connected(int from, int to) {
        lock.lock();
        try {
            if (!(containsNode(from) && containsNode(to))) {
                return false;
            }

            return MoreGraphs.connected(readFromStore(), from, to);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Graph<Integer> getGraph() {
        return readFromStore();
    }

    // Must be executed under lock.
    private boolean containsNode(int nodeId) {
        return store.getNodes().contains(nodeId);
    }

    // Must be executed under lock.
    private MutableGraph<Integer> readFromStore() {
        Set<Integer> nodes = store.getNodes();
        MutableGraph<Integer> graph = GraphBuilder.undirected().expectedNodeCount(nodes.size()).build();
        nodes.forEach(graph::addNode);
        nodes.forEach(from -> store.getEdges(from).forEach(to -> graph.putEdge(from, to)));
        return graph;
    }
}
