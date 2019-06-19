/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import com.google.common.graph.Graph;
import org.immutables.value.Value;

@Value.Immutable
interface VersionedGraph {
    int getSeqId();
    Graph<Integer> graph();
}
