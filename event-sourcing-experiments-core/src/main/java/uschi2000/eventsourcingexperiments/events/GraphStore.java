/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import java.util.Optional;

/**
 * Provides storage and retrieval for immutable, versioned graphs. No assumptions are made regarding the order in
 * which graphs are stored. However, we explicitly keep track of the graph with largest version number.
 * <p>
 * This interface is typically implemented via a key-value store with a simple transaction mechanism to keep track of
 * the largest version, for instance MySQL, S3, or Cassandra.
 */
public interface GraphStore {
    /**
     * Stores the graph under the given version or throws an exception if a graph is already stored for this version.
     */
    void put(VersionedGraph graph);

    /** Returns the graph with the given version if it exists. */
    Optional<VersionedGraph> get(int version);

    /**
     * Returns the <i>largest</i> known graph, i.e., the graph with the largest version number (not necessarily the
     * latest graph added to the store.
     */
    Optional<VersionedGraph> largest();
}
