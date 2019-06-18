/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import org.immutables.value.Value;

public final class MemoryImage implements GraphEvent.Visitor {

    @GuardedBy("graph")
    private final MutableGraph<Integer> graph = GraphBuilder.undirected().build();
    private int seqId = -1;

    @Override
    public void visit(GraphEvent.NodeAdded event) {
        synchronized (graph) {
            graph.addNode(event.getNodeId());
        }
        seqId = event.seqId();  // must be after graph mutation
    }

    @Override
    public void visit(GraphEvent.EdgeAdded event) {
        synchronized (graph) {
            graph.putEdge(event.getFrom(), event.getTo());
        }
        seqId = event.seqId();  // must be after graph mutation
    }

    public VersionedGraph graph() {
        synchronized (graph) {
            return ImmutableVersionedGraph.builder().seqId(seqId).graph(ImmutableGraph.copyOf(graph)).build();
        }
    }

    @Value.Immutable
    interface VersionedGraph {
        int getSeqId();
        ImmutableGraph<Integer> graph();
    }
}
