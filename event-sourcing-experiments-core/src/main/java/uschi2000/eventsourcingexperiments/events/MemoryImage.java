/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a memory image of the graph represented by the sequence of {@link GraphEvent graph mutation events}
 * provided by the given {@link EventStore}. Uses a {@link GraphStore} to persist and hydrate the memory image at
 * periodic intervals.
 */
public final class MemoryImage implements GraphEvent.Visitor {

    @VisibleForTesting
    static final int SNAPSHOT_FREQ = 10;  // persist graphs after every 10 mutations

    private final GraphStore snapshots;
    @GuardedBy("graph")
    private final MutableGraph<Integer> graph;
    private int seqId;
    private final AtomicInteger mutationCounter = new AtomicInteger(0);

    public MemoryImage(EventStore events, GraphStore snapshots) {
        this.snapshots = snapshots;

        // TODO(rfink): In a real distributed system the re-hydration should happen asynchronously and not in the
        //  constructor. We would want to think about cache expiry and memory consumption, etc.
        Optional<VersionedGraph> snapshot = snapshots.largest();
        if (snapshot.isPresent()) {
            this.graph = Graphs.copyOf(snapshot.get().graph());
            this.seqId = snapshot.get().getSeqId();
        } else {
            this.graph = GraphBuilder.undirected().build();
            this.seqId = -1;
        }

        events.subscribe(this, this.seqId);
    }

    @Override
    public void visit(GraphEvent.NodeAdded event) {
        synchronized (graph) {
            graph.addNode(event.getNodeId());
        }
        seqId = event.seqId();  // must be after graph mutation
        maybePersistSnapshot();
    }

    @Override
    public void visit(GraphEvent.EdgeAdded event) {
        synchronized (graph) {
            graph.putEdge(event.getFrom(), event.getTo());
        }
        seqId = event.seqId();  // must be after graph mutation
        maybePersistSnapshot();
    }

    /** Returns the current graph represented by this memory image. */
    public VersionedGraph graph() {
        synchronized (graph) {
            return ImmutableVersionedGraph.builder().seqId(seqId).graph(ImmutableGraph.copyOf(graph)).build();
        }
    }

    // TODO(rfink): In a real system this should happen asynchronously rather than on the request thread.
    private void maybePersistSnapshot() {
        if (mutationCounter.accumulateAndGet(1 /* ignored */, (prev, ignored) -> (prev + 1) % SNAPSHOT_FREQ) == 0) {
            snapshots.put(graph());
        }
    }
}
