/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import java.util.Set;
import uschi2000.eventsourcingexperiments.GraphDb;
import uschi2000.eventsourcingexperiments.brokenevents.BrokenEventGraphDb;
import uschi2000.eventsourcingexperiments.crud.CrudGraphDb;
import uschi2000.eventsourcingexperiments.graphs.MoreGraphs;

/**
 * An event-sourced implementation of the {@link GraphDb graph database}. This implementation fixes the
 * concurrency problems in {@link BrokenEventGraphDb} by
 * introducing transactional coupling between constraint checking (e.g., "is the resulting graph acyclic?") and state
 * mutation (e.g., "add this edge"). The coupling is done by enforcing a dense order of the mutation events: if we
 * check the graph constraints at event sequence number N, then we insert the mutation event at sequence number N+1;
 * by construction, no other mutation can occur in between and thus the constraints are guaranteed to be met after
 * the N+1 mutation.
 * <p>
 * This simple example demonstrates the structural similarities between the CRUD approach (see
 * {@link CrudGraphDb}) and the event-sourced approach: in both cases, we
 * need to linearize state mutations in order to validate the graph constraints and perform the mutation atomically /
 * transactionally. In the CRUD implementation, we chose to linearize mutations with a distributed lock; in the
 * event-sourced implementation, we chose to linearize mutations with a {@link EventStore dense, ordered sequence of
 * mutation events}. Note that this difference is by choice and is not a principled difference between the CRUD and
 * the event-sourced approaches: for instance, we could linearize the mutations with locks in the event-sourced
 * implementation.
 * <p>
 * The optimistic locking approach chosen here exhibits the typical performance penalty when N users perform
 * concurrent mutations: N-1 out of N concurrent mutation attempts fail because their optimistic lock on the sequence
 * number eventually fails; notably, these N-1 attempts have already done the constraint checking, thus wasting a lot
 * of compute time. A better strategy would be to linearize the mutations: locally via a synchronized queue, or in a
 * distributed system via leader election. Interestingly, one of the main selling points of the event-sourced design
 * seems to evaporate (at least in theory): due to the performance penalty of optimistic locking, we should pick a
 * single actor to perform all mutations, or we should use distributed locks to coordinate... just like in a
 * transactional CRUD store.
 */
public final class EventGraphDb implements GraphDb {

    private final EventStore events;
    private final MemoryImage graphImage;

    public EventGraphDb(EventStore events, GraphStore snapshots) {
        this.events = events;
        this.graphImage = new MemoryImage(events, snapshots);
    }

    @Override
    public boolean addNode(int nodeId) {
        boolean eventSubmitted = false;
        while (!eventSubmitted) {
            VersionedGraph currentGraph = graphImage.graph();
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
            VersionedGraph currentGraph = graphImage.graph();
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

            eventSubmitted = events.put(GraphEvent.edgeAdded(from, to, currentGraph.getSeqId() + 1));
        }
        return true;
    }

    @Override
    public Set<Integer> reachable(int seed) {
        return MoreGraphs.reachable(graphImage.graph().graph(), seed);
    }

    @Override
    public Graph<Integer> getGraph() {
        return graphImage.graph().graph();
    }
}
