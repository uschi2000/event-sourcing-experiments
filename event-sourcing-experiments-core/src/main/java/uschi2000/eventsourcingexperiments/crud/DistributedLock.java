/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 */

package uschi2000.eventsourcingexperiments.crud;

/** A dummy interface representation a distributed/shared lock. */
public interface DistributedLock {
    /** See {@Lock}. */
    void lock();

    /** See {@Lock}. */
    void unlock();
}
