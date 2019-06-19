/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.graph.GraphBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class InMemoryGraphStoreTest {

    private static final VersionedGraph G1 =
            ImmutableVersionedGraph.builder()
                    .seqId(1)
                    .graph(GraphBuilder.undirected().build())
                    .build();
    private static final VersionedGraph G2 = ImmutableVersionedGraph.copyOf(G1).withSeqId(2);

    private GraphStore store;

    @Before
    public void before() {
        store = new InMemoryGraphStore();
    }

    @Test
    public void sanity() {
        assertThat(store.get(G1.getSeqId())).isEmpty();
        store.put(G1);
        assertThat(store.get(G1.getSeqId())).contains(G1);

        assertThatThrownBy(() -> store.put(G1)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void emptyGraphHasNoLatest() {
        assertThat(store.largest()).isEmpty();
    }

    @Test
    public void remembersLatestVersion() {
        store.put(G1);
        assertThat(store.largest()).contains(G1);

        store.put(G2);
        assertThat(store.largest()).contains(G2);
        assertThat(store.get(G1.getSeqId())).contains(G1);
        assertThat(store.get(G2.getSeqId())).contains(G2);
    }
}
