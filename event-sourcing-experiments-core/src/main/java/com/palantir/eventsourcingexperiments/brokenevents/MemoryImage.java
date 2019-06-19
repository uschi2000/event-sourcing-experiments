/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.brokenevents;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.palantir.eventsourcingexperiments.events.EventStore;
import com.palantir.eventsourcingexperiments.events.GraphEvent;

public final class MemoryImage implements GraphEvent.Visitor {

    @GuardedBy("graph")
    private final MutableGraph<Integer> graph;
    private int seqId;

    public MemoryImage(EventStore events) {
        this.graph = GraphBuilder.undirected().build();
        this.seqId = -1;

        events.subscribe(this, this.seqId);
    }

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

    /** Returns the current graph represented by this memory image. */
    public Graph<Integer> graph() {
        synchronized (graph) {
            return ImmutableGraph.copyOf(graph);
        }
    }

    /** Returns the largest seen sequence number. */
    public int getSeqId() {
        return seqId;
    }
}
