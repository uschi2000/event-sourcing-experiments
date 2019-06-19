/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.crud;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uschi2000.eventsourcingexperiments.AbstractGraphDbTest;
import uschi2000.eventsourcingexperiments.api.GraphDb;

@RunWith(MockitoJUnitRunner.class)
public final class CrudGraphDbTest extends AbstractGraphDbTest {

    private InMemoryGraphStore store;

    @Override
    protected GraphDb createGraphDb() {
        store = new InMemoryGraphStore();
        return new CrudGraphDb(store, new FakeDistributedLock());
    }
}
