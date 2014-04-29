package org.bonitasoft.engine.tracking;

import java.util.Collections;
import java.util.List;

public final class FlushEvent {

    private final List<Record> records;

    public FlushEvent(final List<Record> records) {
        if (records != null) {
            this.records = records;
        } else {
            this.records = Collections.emptyList();
        }
    }

    public List<Record> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        return "FlushEvent [records.size=" + records.size() + "]";
    }

}
