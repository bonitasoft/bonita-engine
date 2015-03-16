/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import java.lang.management.MemoryUsage;

import com.bonitasoft.engine.monitoring.SMemoryUsage;

/**
 * @author Matthieu Chaffotte
 */
public class SMemoryUsageImpl implements SMemoryUsage {

    private final long committed;

    private final long max;

    private final long init;

    private final long used;

    public SMemoryUsageImpl(final MemoryUsage memoryUsage) {
        committed = memoryUsage.getCommitted();
        max = memoryUsage.getMax();
        init = memoryUsage.getInit();
        used = memoryUsage.getUsed();
    }

    @Override
    public long getCommitted() {
        return committed;
    }

    @Override
    public long getMax() {
        return max;
    }

    @Override
    public long getInit() {
        return init;
    }

    @Override
    public long getUsed() {
        return used;
    }

}
