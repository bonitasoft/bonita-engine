/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import java.util.Map;

import com.bonitasoft.engine.monitoring.GcInfo;
import com.bonitasoft.engine.monitoring.MemoryUsage;

/**
 * @author Matthieu Chaffotte
 */
public class GcInfoImpl implements GcInfo {

    private static final long serialVersionUID = 2607730148377364541L;

    private final long startTime;

    private final long endTime;

    private final long duration;

    private final Map<String, MemoryUsage> memoryUsageBeforeGc;

    private final Map<String, MemoryUsage> memoryUsageAfterGc;

    public GcInfoImpl(final long startTime, final long endTime, final long duration, final Map<String, MemoryUsage> memoryUsageBeforeGc,
            final Map<String, MemoryUsage> memoryUsageAfterGc) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.memoryUsageBeforeGc = memoryUsageBeforeGc;
        this.memoryUsageAfterGc = memoryUsageAfterGc;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public Map<String, MemoryUsage> getMemoryUsageBeforeGc() {
        return memoryUsageBeforeGc;
    }

    @Override
    public Map<String, MemoryUsage> getMemoryUsageAfterGc() {
        return memoryUsageAfterGc;
    }

}
