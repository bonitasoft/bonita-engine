/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.tracking.memory;

import org.bonitasoft.engine.tracking.AbstractFlushEventListener;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListenerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryFlushEventListener extends AbstractFlushEventListener {

    private static final Logger log = LoggerFactory.getLogger(MemoryFlushEventListener.class);
    private final int maxSize;
    private DayRecord dayRecord;

    public MemoryFlushEventListener(final boolean activateAtStart, final int maxSize) {
        super(activateAtStart);
        this.maxSize = maxSize;
    }

    @Override
    public synchronized FlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {

        if (flushEvent.getRecords().size() == 0) {
            return new FlushEventListenerResult(flushEvent);
        }

        log.debug("Reusing csv file: FlushEvent received with {}   records.", flushEvent.getRecords().size());

        final long flushTime = flushEvent.getFlushTime();
        if (this.dayRecord == null || !this.dayRecord.isExpectedDayKey(flushTime)) {
            this.dayRecord = new DayRecord(flushTime, this.maxSize);
        }
        this.dayRecord.addRecords(flushEvent.getRecords());

        log.info("Adding '{}' records to DayRecord with dayKey '{}'", flushEvent.getRecords().size(),
                this.dayRecord.getDayKey());

        return new FlushEventListenerResult(flushEvent);
    }

    @Override
    public String getStatus() {
        String status = super.getStatus() + "\n";
        if (this.dayRecord == null) {
            status += "No DayRecord registered in memory.";
        } else {
            status += ", dayRecord: " + this.dayRecord.toString();
        }
        return status;
    }

    @Override
    public void notifyStopTracking() {
        this.dayRecord = null;
    }

    @Override
    public void notifyStartTracking() {
        //nothing to do
    }

    public synchronized DayRecord getDayRecord() {
        return this.dayRecord;
    }

    public synchronized void clear() {
        this.dayRecord = null;
    }

}
