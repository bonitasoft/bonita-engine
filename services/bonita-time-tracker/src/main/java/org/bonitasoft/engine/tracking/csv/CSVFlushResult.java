package org.bonitasoft.engine.tracking.csv;

import java.io.File;

import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushResult;

public class CSVFlushResult extends FlushResult {

    private final File outputFile;

    public CSVFlushResult(final FlushEvent flushEvent, final File outputFile) {
        super(flushEvent);
        this.outputFile = outputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

}
