/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.monitoring.impl;

import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.monitoring.SGcInfo;
import org.bonitasoft.engine.monitoring.SMemoryUsage;

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
