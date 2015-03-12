/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import com.bonitasoft.engine.monitoring.MemoryUsage;

/**
 * @author Matthieu Chaffotte
 */
public class MemoryUsageImpl implements MemoryUsage {

    private static final long serialVersionUID = -8661348619247633447L;

    private final long committed;

    private final long max;

    private final long init;

    private final long used;

    public MemoryUsageImpl(final long committed, final long max, final long init, final long used) {
        super();
        this.committed = committed;
        this.max = max;
        this.init = init;
        this.used = used;
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
