package org.bonitasoft.engine.tracking.collector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushEventListenerResult;
import org.bonitasoft.engine.tracking.Record;

public class MemoryFlushEventListener implements FlushEventListener {

    // format is <date, listOfRecord>.
    // //Date format is "20140432"
    public HashMap<String, ArrayList<Record>> collectors = new HashMap<String, ArrayList<Record>>();

    @Override
    public FlushEventListenerResult flush(final TechnicalLoggerService logger, final FlushEvent flushEvent) throws Exception {

        if (flushEvent.getRecords().size() == 0) {
            return new FlushEventListenerResult(flushEvent);
        }

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "org.bonitasoft.engine.tracking.collector.CollectorFlushEvent.flush : ____________________________" + this + " Receive a FlushEvent [" + flushEvent.getRecords().size() + "]");
        }
        // keep all theses new event
        final List<Record> records = flushEvent.getRecords();
        final Calendar currentDate = Calendar.getInstance();
        final String key = String.valueOf(currentDate.get(Calendar.YEAR)) + String.valueOf(currentDate.get(Calendar.DAY_OF_YEAR));
        ArrayList<Record> listOfDay = collectors.get(key);
        if (listOfDay == null) {
            listOfDay = new ArrayList<Record>();
        }
        listOfDay.addAll(records);
        collectors.put(key, listOfDay);
        if (collectors.size() > 1) {
            // purge old days
            collectors.clear();
            collectors.put(key, listOfDay);
        }

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "org.bonitasoft.engine.tracking.collector.CollectorFlushEvent.flush :________________  Collects [" + flushEvent.getRecords().size()
                    + "] tank:[" + collectors.size() + "] Day[" + key + "] nbInDay[" + collectors.get(key).size() + "]");
        }
        return new FlushEventListenerResult(flushEvent);
    }

    @Override
    public String getStatus() {
        return null;
    }

    public List<Record> getRecords() {
        final Logger logger = Logger.getLogger("org.bonitasoft");
        final Calendar currentDate = Calendar.getInstance();
        final String key = String.valueOf(currentDate.get(Calendar.YEAR)) + String.valueOf(currentDate.get(Calendar.DAY_OF_YEAR));
        final List<Record> listRecord = collectors.get(key);
        logger.info("org.bonitasoft.engine.tracking.collector.CollectorFlushEvent.getRecords: ________________: " + this + " Collector [" + collectors.size()
                + "] key[" + key + "] nbInDay["
                + (collectors.get(key) == null ? "null" : collectors.get(key).size()) + "]");

        return listRecord == null ? new ArrayList<Record>() : listRecord;
    }

    public void clear() {
        collectors.clear();
    }

}