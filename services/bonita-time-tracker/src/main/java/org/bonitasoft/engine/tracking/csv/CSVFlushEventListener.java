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
package org.bonitasoft.engine.tracking.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.AbstractFlushEventListener;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.Record;

public class CSVFlushEventListener extends AbstractFlushEventListener {

    private final String csvSeparator;

    private final String outputFolder;

    public static final String FILE_PREFIX = "bonita_timetracker_";

    public static final String FILE_SUFFIX = ".csv";

    public CSVFlushEventListener(final boolean activateAtStart, final TechnicalLoggerService logger,
            final String outputFolder, final String csvSeparator) {
        super(activateAtStart, logger);
        this.outputFolder = outputFolder;
        this.csvSeparator = csvSeparator;

        final File outputFolderFile = new File(outputFolder);
        if (!outputFolderFile.exists()) {
            throw new RuntimeException("Output folder does not exist: " + outputFolder);
        }
        if (!outputFolderFile.isDirectory()) {
            throw new RuntimeException("Output folder is not a directory: " + outputFolder);
        }
    }

    @Override
    public CSVFlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {
        final long flushTime = flushEvent.getFlushTime();
        final List<Record> records = flushEvent.getRecords();
        final List<List<String>> csvContent = new ArrayList<List<String>>();
        for (final Record record : records) {
            final List<String> row = getRow(record);
            csvContent.add(row);
        }

        final File outputFile = getDayFile(flushTime, this.outputFolder, FILE_PREFIX, FILE_SUFFIX);

        if (!outputFile.exists()) {
            log(TechnicalLogSeverity.INFO, "Generating new csv file to: " + outputFile);
            CSVUtil.writeCSVRow(outputFile, getHeaderRow(), this.csvSeparator);
        } else {
            log(TechnicalLogSeverity.INFO, "Reusing csv file: " + outputFile);
        }
        CSVUtil.writeCSVRows(outputFile, csvContent, this.csvSeparator);
        return new CSVFlushEventListenerResult(flushEvent, outputFile);
    }

    @Override
    public String getStatus() {
        String status = super.getStatus() + "\n";
        status += "outputFolder: " + this.outputFolder + "\n";
        return status;
    }

    @Override
    public void notifyStopTracking() {
        //nothing to do
    }

    @Override
    public void notifyStartTracking() {
        //nothing to do
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
        final int millisecond = cal.get(Calendar.MILLISECOND);

        final List<String> row = new ArrayList<String>();
        row.add(String.valueOf(timestamp));
        row.add(String.valueOf(year));
        row.add(String.valueOf(month));
        row.add(String.valueOf(dayOfMonth));
        row.add(String.valueOf(hourOfDay));
        row.add(String.valueOf(minute));
        row.add(String.valueOf(second));
        row.add(String.valueOf(millisecond));
        row.add(String.valueOf(record.getDuration()));
        row.add(record.getName().name());
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
        header.add("millisecond");
        header.add("duration");
        header.add("name");
        header.add("description");
        return header;
    }

    private String getIntOnTwoNumbers(final int i) {
        if (i < 10) {
            return "0" + i;
        }
        return Integer.toString(i);
    }

    private File getDayFile(final long time, final String folder, final String filePrefix, final String fileSuffix) {
        final StringBuilder sb = new StringBuilder();
        final GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(time);

        sb.append(getIntOnTwoNumbers(c.get(Calendar.YEAR)));
        sb.append("_");
        sb.append(getIntOnTwoNumbers(c.get(Calendar.MONTH) + 1));
        sb.append("_");
        sb.append(getIntOnTwoNumbers(c.get(Calendar.DAY_OF_MONTH)));

        final String timestamp = sb.toString();
        final String fileName = filePrefix + timestamp + fileSuffix;

        return new File(folder, fileName);
    }

}
