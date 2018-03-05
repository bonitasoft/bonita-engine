/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.AbstractTimeTrackerTest;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.RecordAssert;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CSVFlushEventListenerTest extends AbstractTimeTrackerTest {

    private static final TimeTrackerRecords TIME_TRACKER_RECORDS = TimeTrackerRecords.EVALUATE_EXPRESSION;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TechnicalLoggerService logger;

    @Test
    public void should_work_if_output_folder_is_a_folder() throws Exception {
        new CSVFlushEventListener(true, logger, temporaryFolder.newFolder().getAbsolutePath(), ";");
    }

    @Test
    public void should_fail_if_output_folder_unknown() {
        //then
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Output folder does not exist");

        //when
        new CSVFlushEventListener(true, logger, "unknownFolder", ";");
    }

    @Test
    public void should_fail_if_outputfolder_is_a_file() throws Exception {
        //then
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Output folder is not a directory");

        //when
        new CSVFlushEventListener(true, logger, temporaryFolder.newFile().getAbsolutePath(), ";");
    }

    @Test
    public void flushedCsv() throws Exception {
        //given
        final CSVFlushEventListener csvFlushEventListener = new CSVFlushEventListener(true, logger, temporaryFolder.newFolder().getAbsolutePath(), ";");
        final Record rec1 = new Record(System.currentTimeMillis(), TIME_TRACKER_RECORDS, "rec1Desc", 100);
        final Record rec2 = new Record(System.currentTimeMillis(), TIME_TRACKER_RECORDS, "rec2Desc", 200);

        //when
        final CSVFlushEventListenerResult csvFlushResult = csvFlushEventListener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec1, rec2)));
        final File csvFile = csvFlushResult.getOutputFile();

        //then
        final List<List<String>> csvValues = CSVUtil.readCSV(true, csvFile, ";");
        assertThat(csvValues).as("should contains 2 records").hasSize(2);
        checkCSVRecord(rec1, csvValues.get(0));
        checkCSVRecord(rec2, csvValues.get(1));

        final List<Record> records = csvFlushResult.getFlushEvent().getRecords();
        assertThat(records).as("should contains 2 records").hasSize(2);
        checkRecord(rec1, records.get(0));
        checkRecord(rec2, records.get(1));
    }

    private void checkCSVRecord(final Record record, final List<String> csvValues) {
        // timestamp, year, month, day, hour, minute, second, millisecond, duration, name, description]
        assertThat(csvValues).hasSize(11);

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

        RecordAssert.assertThat(record)
                .hasTimestamp(Long.valueOf(csvValues.get(0)).longValue())
                .hasDuration(Long.valueOf(csvValues.get(8)).longValue())
                .hasDescription(csvValues.get(10))
                .hasName(TimeTrackerRecords.EVALUATE_EXPRESSION);

        assertThat(timestamp).isEqualTo(Long.valueOf(csvValues.get(0)).longValue());
        assertThat(year).isEqualTo(Integer.valueOf(csvValues.get(1)).intValue());
        assertThat(month).isEqualTo(Integer.valueOf(csvValues.get(2)).intValue());
        assertThat(dayOfMonth).isEqualTo(Integer.valueOf(csvValues.get(3)).intValue());
        assertThat(hourOfDay).isEqualTo(Integer.valueOf(csvValues.get(4)).intValue());
        assertThat(minute).isEqualTo(Integer.valueOf(csvValues.get(5)).intValue());
        assertThat(second).isEqualTo(Integer.valueOf(csvValues.get(6)).intValue());
        assertThat(millisecond).isEqualTo(Integer.valueOf(csvValues.get(7)).intValue());

    }

}
