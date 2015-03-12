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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.bonitasoft.engine.monitoring.SGcInfo;
import com.bonitasoft.engine.monitoring.SMemoryUsage;
import com.sun.management.GcInfo;

/**
 * @author Matthieu Chaffotte
 */
public class SGcInfoImpl implements SGcInfo {

    private final long startTime;

    private final long endTime;

    private final long duration;

    private final Map<String, SMemoryUsage> memoryUsageBeforeGc;

    private final Map<String, SMemoryUsage> memoryUsageAfterGc;

    @SuppressWarnings("restriction")
    public SGcInfoImpl(final GcInfo gcInfo) {
        startTime = gcInfo.getStartTime();
        endTime = gcInfo.getEndTime();
        duration = gcInfo.getDuration();
        memoryUsageBeforeGc = new HashMap<String, SMemoryUsage>();
        final Map<String, MemoryUsage> beforeGc = gcInfo.getMemoryUsageBeforeGc();
        for (final Entry<String, MemoryUsage> mem : beforeGc.entrySet()) {
            memoryUsageBeforeGc.put(mem.getKey(), new SMemoryUsageImpl(mem.getValue()));
        }
        memoryUsageAfterGc = new HashMap<String, SMemoryUsage>();
        final Map<String, MemoryUsage> afterGc = gcInfo.getMemoryUsageAfterGc();
        for (final Entry<String, MemoryUsage> mem : afterGc.entrySet()) {
            memoryUsageAfterGc.put(mem.getKey(), new SMemoryUsageImpl(mem.getValue()));
        }
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
    public Map<String, SMemoryUsage> getMemoryUsageBeforeGc() {
        return memoryUsageBeforeGc;
    }

    @Override
    public Map<String, SMemoryUsage> getMemoryUsageAfterGc() {
        return memoryUsageAfterGc;
    }

}
