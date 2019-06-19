/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.events;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uschi2000.eventsourcingexperiments.AbstractGraphDbTest;
import uschi2000.eventsourcingexperiments.GraphDb;

@RunWith(MockitoJUnitRunner.class)
public final class EventGraphDbTest extends AbstractGraphDbTest {

    @Override
    protected GraphDb createGraphDb() {
        return new EventGraphDb(new InMemoryEventStore(), new InMemoryGraphStore());
    }
}
