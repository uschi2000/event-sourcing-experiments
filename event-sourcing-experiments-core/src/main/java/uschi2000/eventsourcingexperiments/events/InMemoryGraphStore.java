/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class InMemoryGraphStore implements GraphStore {

    private final ConcurrentNavigableMap<Integer, VersionedGraph> store = new ConcurrentSkipListMap<>();

    @Override
    public void put(VersionedGraph graph) {
        if (store.putIfAbsent(graph.getSeqId(), graph) != null) {
            throw new RuntimeException("A graph already exists for version: " + graph.getSeqId());
        }
    }

    @Override
    public Optional<VersionedGraph> get(int version) {
        return Optional.ofNullable(store.get(version));
    }

    @Override
    public Optional<VersionedGraph> largest() {
        return Optional.ofNullable(store.lastEntry()).map(Map.Entry::getValue);
    }
}
