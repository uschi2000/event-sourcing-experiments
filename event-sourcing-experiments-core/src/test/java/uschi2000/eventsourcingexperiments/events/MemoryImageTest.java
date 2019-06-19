/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class MemoryImageTest {

    @Mock private EventStore events;
    @Mock private GraphStore snapshots;

    @Test
    public void canRehydrateFromSnapshot() {
        MutableGraph<Integer> graph = GraphBuilder.undirected().build();
        graph.addNode(0);
        graph.addNode(1);
        ImmutableVersionedGraph versionedGraph = ImmutableVersionedGraph.builder()
                .seqId(1)
                .graph(graph)
                .build();
        when(snapshots.largest()).thenReturn(Optional.of(versionedGraph));

        MemoryImage image = new MemoryImage(events, snapshots);
        assertThat(image.graph()).isEqualTo(versionedGraph);
        verify(events).subscribe(image, 1);

        image.visit(GraphEvent.nodeAdded(2, 2));
        assertThat(image.graph().graph().nodes()).contains(0, 1, 2);
    }

    @Test
    public void canRehydrateFromEvents() {
        when(snapshots.largest()).thenReturn(Optional.empty());

        MemoryImage image = new MemoryImage(events, snapshots);
        image.visit(GraphEvent.nodeAdded(0, 0));
        image.visit(GraphEvent.nodeAdded(1, 1));

        verify(events).subscribe(image, -1);
        assertThat(image.graph().graph().nodes()).contains(0, 1);
    }

    @Test
    public void persistsSnapshotsPeriodically() {
        when(snapshots.largest()).thenReturn(Optional.empty());
        MemoryImage image = new MemoryImage(events, snapshots);

        for (int i = 0; i < MemoryImage.SNAPSHOT_FREQ; ++i) {
            image.visit(GraphEvent.nodeAdded(i, i));
            if (i < MemoryImage.SNAPSHOT_FREQ - 1) {
                verify(snapshots, never()).put(any());
            }
        }
        ArgumentCaptor<VersionedGraph> graph = ArgumentCaptor.forClass(VersionedGraph.class);
        verify(snapshots).put(graph.capture());
        assertThat(graph.getValue().graph().nodes()).hasSize(10);
    }
}
