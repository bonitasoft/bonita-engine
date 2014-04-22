package org.bonitasoft.engine.tracking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class RecordKeeper {

    private final String csvSeparator;

    private final String outputFolder;

    private final Queue<Record> records;

    private final List<FlushEventListener> flushSubscribers;

    private final TechnicalLoggerService logger;

    public RecordKeeper(final TechnicalLoggerService logger, final int maxSize, final String csvSeparator, final String outputFolder,
            final List<FlushEventListener> flushSubscribers) {
        this.logger = logger;
        this.csvSeparator = csvSeparator;
        this.outputFolder = outputFolder;
        this.flushSubscribers = flushSubscribers;
        this.records = new CircularFifoQueue<Record>(maxSize);
    }

    public void record(final Record record) {
        // TODO needs a synchro?
        records.add(record);
    }

    public List<Record> cloneRecords() {
        return Arrays.asList(this.records.toArray(new Record[] {}));
    }

    public FlushEvent flush() throws IOException {
        final List<Record> records = cloneRecords();
        final List<List<String>> csvContent = new ArrayList<List<String>>();
        csvContent.add(getHeaderRow());
        for (final Record record : records) {
            final List<String> row = getRow(record);
            csvContent.add(row);
        }
        final String timestamp = CSVUtil.getFileTimestamp(System.currentTimeMillis());

        final File outputFile = new File(outputFolder, timestamp + "_bonita_timetracker_" + UUID.randomUUID().toString() + ".csv");

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Generating csv file to: " + outputFile);
        }
        CSVUtil.writeCSV(outputFile, csvContent, csvSeparator);

        final FlushEvent flushEvent = new FlushEvent(records, outputFile);

        if (this.flushSubscribers != null) {
            for (final FlushEventListener subscriber : this.flushSubscribers) {
                try {
                    subscriber.flush(flushEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return flushEvent;
    }

    private List<String> getRow(final Record record) {
        final long timestamp = record.getTimestamp();
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int milisecond = cal.get(Calendar.MILLISECOND);

        final List<String> row = new ArrayList<String>();
        row.add(String.valueOf(timestamp));
        row.add(String.valueOf(year));
        row.add(String.valueOf(month));
        row.add(String.valueOf(dayOfMonth));
        row.add(String.valueOf(hourOfDay));
        row.add(String.valueOf(minute));
        row.add(String.valueOf(second));
        row.add(String.valueOf(milisecond));
        row.add(String.valueOf(record.getDuration()));
        row.add(record.getName());
        row.add(record.getDescription());
        return row;
    }

    private List<String> getHeaderRow() {
        final List<String> header = new ArrayList<String>();
        header.add("timestamp");
        header.add("year");
        header.add("month");
        header.add("dayOfMonth");
        header.add("hourOfDay");
        header.add("minute");
        header.add("second");
        header.add("milisecond");
        header.add("duration");
        header.add("name");
        header.add("description");
        return header;
    }
}
