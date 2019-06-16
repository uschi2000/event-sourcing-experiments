/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.crud;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class InMemoryGraphStore implements GraphStore {

    private final Set<Integer> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Multimap<Integer, Integer> edgesFrom =
            Multimaps.synchronizedMultimap(MultimapBuilder.treeKeys().treeSetValues().build());

    @Override
    public void addNode(int nodeId) {
        nodes.add(nodeId);
    }

    @Override
    public Set<Integer> getNodes() {
        return ImmutableSet.copyOf(nodes);
    }

    @Override
    public void addEdge(int from, int to) {
        edgesFrom.put(from, to);
    }

    @Override
    public Set<Integer> getEdges(int from) {
        return ImmutableSet.copyOf(edgesFrom.get(from));
    }
}
