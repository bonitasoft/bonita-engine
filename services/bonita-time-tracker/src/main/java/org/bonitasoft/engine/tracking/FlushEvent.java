package org.bonitasoft.engine.tracking;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class FlushEvent {

    private final File outputFile;

    private final List<Record> records;

    public FlushEvent(final List<Record> records, final File outputFile) {
        if (records != null) {
            this.records = records;
        } else {
            this.records = Collections.emptyList();
        }
        this.outputFile = outputFile;
    }

    public List<Record> getRecords() {
        return records;
    }

    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public String toString() {
        return "FlushEvent [outputFile=" + outputFile + ", records.size=" + records.size() + "]";
    }

}
