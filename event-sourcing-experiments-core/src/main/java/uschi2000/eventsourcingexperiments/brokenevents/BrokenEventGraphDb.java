/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.brokenevents;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import uschi2000.eventsourcingexperiments.api.GraphDb;
import uschi2000.eventsourcingexperiments.events.EventStore;
import uschi2000.eventsourcingexperiments.events.GraphEvent;
import uschi2000.eventsourcingexperiments.graphs.MoreGraphs;

/** A first, broken attempt at an event-sourced graph database. This database has a number of race conditions that */
public final class BrokenEventGraphDb implements GraphDb {

    private final EventStore events;
    private final MemoryImage graphImage;

    public BrokenEventGraphDb(EventStore events) {
        this.events = events;
        this.graphImage = new MemoryImage(events);
    }

    @Override
    public boolean addNode(int nodeId) {
        if (getGraph().nodes().contains(nodeId)) {
            return false;
        }

        // Note that we violate the API contract here: we may fail to add the edge if the sequence numbers don't line up
        return events.put(GraphEvent.nodeAdded(nodeId, graphImage.getSeqId() + 1));
    }

    @Override
    public boolean addEdgeAcyclic(int from, int to) {
        Graph<Integer> currentGraph = graphImage.graph();
        if (!(currentGraph.nodes().contains(from) && currentGraph.nodes().contains(to))) {
            return false;
        }

        if (currentGraph.hasEdgeConnecting(from, to)) {
            return false;
        }

        MutableGraph<Integer> maybeNewGraph = Graphs.copyOf(currentGraph);
        maybeNewGraph.putEdge(from, to);
        if (Graphs.hasCycle(maybeNewGraph)) {
            return false;
        }

        // Note that we violate the API contract here: we may fail to add the edge if the sequence numbers don't line up
        return events.put(GraphEvent.edgeAdded(from, to, graphImage.getSeqId() + 1));
    }

    @Override
    public boolean connected(int from, int to) {
        return MoreGraphs.connected(graphImage.graph(), from, to);
    }

    @Override
    public Graph<Integer> getGraph() {
        return graphImage.graph();
    }
}
