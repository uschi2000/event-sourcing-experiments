/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.events;

import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.palantir.eventsourcingexperiments.api.GraphDb;
import com.palantir.eventsourcingexperiments.graphs.MoreGraphs;

public final class EventGraphDb implements GraphDb {

    private final EventStore events;
    private final MemoryImage graphImage;

    public EventGraphDb(EventStore events, MemoryImage graphImage) {
        this.events = events;
        this.graphImage = graphImage;

        events.subscribe(graphImage);
    }

    @Override
    public boolean addNode(int nodeId) {
        boolean eventSubmitted = false;
        while (!eventSubmitted) {
            MemoryImage.VersionedGraph currentGraph = graphImage.graph();
            if (currentGraph.graph().nodes().contains(nodeId)) {
                // Note that this is a bit funny for non-monotonic graphs: we fail here as soon as we observe a graph
                // that contains nodeId, even though that node may get removed later... how long are you willing to
                // wait? Good demonstration that all of these graph mutations operate with "point in time" consistency,
                // not "absolutely" consistency. (Well, there is no such thing, because different users can observe
                // synchronized time.)
                return false;
            }

            eventSubmitted = events.put(GraphEvent.nodeAdded(nodeId, currentGraph.getSeqId() + 1));
        }
        return true;
    }

    @Override
    public boolean addEdgeAcyclic(int from, int to) {
        boolean eventSubmitted = false;
        while (!eventSubmitted) {
            MemoryImage.VersionedGraph currentGraph = graphImage.graph();
            if (!(currentGraph.graph().nodes().contains(from) && currentGraph.graph().nodes().contains(to))) {
                return false;
            }

            if (currentGraph.graph().hasEdgeConnecting(from, to)) {
                return false;
            }

            MutableGraph<Integer> maybeNewGraph = Graphs.copyOf(currentGraph.graph());
            maybeNewGraph.putEdge(from, to);
            if (Graphs.hasCycle(maybeNewGraph)) {
                return false;
            }

            // The following construction couples the state checking of graph (contains nodes, does not contain edge,
            // is acyclic) with the state mutation: we check the constraints on a graph at version seqId and then insert
            // the edgeAdded event at seqId + 1. By construction, no other mutation can be inserted in between seqId
            // and seqId + 1 and thus we're guaranteed to update the graph correctly.
            eventSubmitted = events.put(GraphEvent.edgeAdded(from, to, currentGraph.getSeqId() + 1));
        }
        return true;
    }

    @Override
    public boolean connected(int from, int to) {
        return MoreGraphs.connected(graphImage.graph().graph(), from, to);
    }
}
