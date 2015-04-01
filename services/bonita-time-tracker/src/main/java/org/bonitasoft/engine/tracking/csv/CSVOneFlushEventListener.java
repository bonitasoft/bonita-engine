 	/**
		 * Copyright (C) 2014 BonitaSoft S.A.
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

		import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushResult;
import org.bonitasoft.engine.tracking.Record;

		public class CSVOneFlushEventListener implements FlushEventListener {

		    private final String csvSeparator;

		    private final String outputFolder;

		    private final TechnicalLoggerService logger;

		    public CSVOneFlushEventListener(final TechnicalLoggerService logger, final String outputFolder, final String csvSeparator) {
		        super();
		        this.logger = logger;
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

		    /**
		     * @override
		     */
		    public FlushResult flush(final FlushEvent flushEvent) throws Exception  {
		        final List<Record> records = flushEvent.getRecords();
		        
		        final String timestamp = getFileTimestampDay(System.currentTimeMillis());
		        final File outputFile = new File(outputFolder, timestamp + "_bonita_timetracker.csv");
		        boolean exist = outputFile.exists();

		        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
		            logger.log(getClass(), TechnicalLogSeverity.INFO, "Generating csv file to: " + outputFile+" append: "+exist);
		        }
		            
				    final FileWriter writer = new FileWriter(outputFile, true);
		        if (!exist)
		        {
		        		// header
        				boolean first = true;
        				for (String value : getHeaderRow())
        				{
        						if (! first) {
                        writer.append(csvSeparator);
        						}
                    writer.append(value);
                    first = false;
        				}
                writer.append("\n");
		        }
		        
		        // write all value now
		        for (final Record record : records) {
        				boolean first = true;
		        		for (String value :  getRow(record))
		        				{
		        						if (! first) {
		                        writer.append(csvSeparator);
		        						}
		                    writer.append(value);
		                    first = false;
		        				}
		                writer.append("\n");
		        }
		        writer.close();
		        
		        return new CSVFlushResult(flushEvent, outputFile);
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

		    /**
		     * return a file tiome stamp base on Day
		     * @param time
		     * @return
		     */
		    public static String getFileTimestampDay(final long time) {
		    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		    		return simpleDateFormat.format( new Date( time ));
		    }
		}

