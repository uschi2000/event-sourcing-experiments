/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.eventsourcingexperiments.crud;

import com.palantir.eventsourcingexperiments.AbstractGraphDbTest;
import com.palantir.eventsourcingexperiments.api.GraphDb;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class CrudGraphDbTest extends AbstractGraphDbTest {

    private InMemoryGraphStore store;

    @Override
    protected GraphDb createGraphDb() {
        store = new InMemoryGraphStore();
        return new CrudGraphDb(store, new FakeDistributedLock());
    }
}
