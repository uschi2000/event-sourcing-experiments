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
    @Override
    protected GraphDb createGraphDb() {
        return new CrudGraphDb(new InMemoryGraphStore(), new FakeDistributedLock());
    }
}