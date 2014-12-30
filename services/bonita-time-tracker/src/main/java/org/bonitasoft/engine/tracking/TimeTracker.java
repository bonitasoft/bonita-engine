package org.bonitasoft.engine.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushResult;
import org.bonitasoft.engine.tracking.Record;

public class TimeTracker implements TenantLifecycleService {

		/** activatedRecord is the list of the different kind of record to track
		 * It may change */
    private Set<String> activatedRecords;

    private final FlushThread flushThread;

    // TODO avec Charles : how keep this syntax without an Syntax error ?
    private final List<? extends FlushEventListener> flushEventListeners;
    // private List<FlushEventListener> flushEventListeners;
    
    
    private final TechnicalLoggerService logger;

    /**
     * the first queue contains only the "will be flush" event.
     */
    private final ConcurrentLinkedQueue<Record> recordsInQueue;
    
    /**
     * The queue contains all the last N records
     * NB: to avoid the synchronization, it may be a better idea to create one EventFlusher, and then the Event Flusher keep the list.
     */
    private final Queue<Record> allRecords;

    private boolean started;

    private final boolean startFlushThread;

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean startFlushThread,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        this(logger, new ThreadSleepClockImpl(), startFlushThread, flushEventListeners, maxSize, flushIntervalInSeconds * 1000, activatedRecords);
    }

    public TimeTracker(
            final TechnicalLoggerService logger,
            final Clock clock,
            final boolean startFlushThread,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        super();
        this.startFlushThread = startFlushThread;
        started = false;
        this.logger = logger;
        this.flushEventListeners = flushEventListeners;
        if (activatedRecords == null || activatedRecords.length == 0) {
            this.activatedRecords = Collections.emptySet();
        } else {
            this.activatedRecords = new HashSet<String>(Arrays.asList(activatedRecords));
        }
        if (activatedRecords != null && activatedRecords.length > 0 && logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            this.logger.log(getClass(), TechnicalLogSeverity.INFO,
                    "Time tracker is activated for some records. This may not be used in production as performances may be strongly impacted: "
                            + activatedRecords);
        }
        this.flushThread = new FlushThread(clock, flushIntervalInSeconds, this, logger);

        this.recordsInQueue = new ConcurrentLinkedQueue<Record>();
        this.allRecords = new CircularFifoQueue<Record>(maxSize);
    }

    public boolean isTrackable(final String recordName) {
        return started && this.activatedRecords.contains(recordName);
    }

    public void track(final String recordName, final String recordDescription, final long duration) {
        if (isTrackable(recordName)) {
            final long timestamp = System.currentTimeMillis();
            final Record record = new Record(timestamp, recordName, recordDescription, duration);
            track(record);
        }
    }

    public void track(final Record record) {
        if (isTrackable(record.getName())) {
            if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Tracking record: " + record);
            }
            recordsInQueue.add(record);
            
            // Evolution : keep a reference to a Flusher, and ask him. Then, the synchonisation is not needed
            synchronized ( allRecords ) {
            		allRecords.add(record);
						}
        }
    }

    public List<FlushResult> flush() throws IOException {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flushing...");
        }
        final List<Record> listRecords = new ArrayList<Record>();
        Record oneRecord =null;
        do
        {
        		oneRecord = recordsInQueue.poll();
        		if (oneRecord!=null)
        				listRecords.add( oneRecord );
        } while (oneRecord !=null);

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Flushing...");
        }

        final List<FlushResult> flushResults = new ArrayList<FlushResult>();

        // build the flushEvent array from the queue. Only the "not already flushed" event are flushed.
        if ((listRecords.size()>0) && (flushEventListeners != null))
        {
        	final FlushEvent flushEvent = new FlushEvent(listRecords);
         
          logger.log(getClass(), TechnicalLogSeverity.INFO, "Flushing... to ["+flushEventListeners.size()+"]");
          
          for (final FlushEventListener listener : flushEventListeners) {
              try {
                  flushResults.add(listener.flush(flushEvent));
              } catch (final Exception e) {
                  if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                      logger.log(getClass(), TechnicalLogSeverity.WARNING, "Exception while flushing: " + flushEvent + " on listener " + listener);
                  }
              }
          }
          
          if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
              logger.log(getClass(), TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
          }
        }
        return flushResults;
    }

    /**
     * the getRecords return ALL the records (managed in a circular queue, so you may lost some records).
     */
    public List<Record> getRecords() throws IOException {
        return Arrays.asList(this.records.toArray(new Record[] {}));
    }

    @Override
    public void start() throws SBonitaException {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting TimeTracker...");
        }
        if (this.startFlushThread && !this.flushThread.isAlive()) {
            this.flushThread.start();
        }
        this.started = true;
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker started.");
        }
    }

    @Override
    public void stop() throws SBonitaException {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Stopping TimeTracker...");
        }
        
        // the interrupt() does not work, so double the mechanism by a manuel stop 
        this.flushThread.askStop();
        if (this.flushThread.isAlive()) {
            this.flushThread.interrupt();
        }
        this.started = false;
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker stopped.");
        }
    }

    @Override
    public void pause() throws SBonitaException {
        // nothing to do as this service is not for production, we don't want to spend time on this
    }

    @Override
    public void resume() throws SBonitaException {
        // nothing to do as this service is not for production, we don't want to spend time on this
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFlushThreadAlive() {
        return this.flushThread.isAlive();
    }
    
    /**
     * return the list of different activated record.
     * Different string have constants, and can be found at org.bonitasoft.engine.tracking.TimeTrackerRecords
     * @return
     * @see org.bonitasoft.engine.tracking.TimeTrackerRecords
     */
    public Set<String> getActivatedRecords()
    {
    	return activatedRecords;
    }
    
    /**
     * Set the list of different string.   
     * @param activatedRecords
     * @see org.bonitasoft.engine.tracking.TimeTrackerRecords
     * @see 
     */
    public void setActivatedRecords(Set<String> activatedRecords )
    {
    		this.activatedRecords = activatedRecords;
    }
    
    /**
     * Add a new register event 
     * @param flushEventListener
     */
    public void registerListener(FlushEventListener flushEventListener )
    { 
    		this.flushEventListeners.add( flushEventListener );
    }
    
    /**
     * unregister an event listener.
     */
    public void unRegisterListener(FlushEventListener flushEventListener )
    { 
    		this.flushEventListeners.remove( flushEventListener );
    }
    
    
    /**
     * return all eventListeners
     */
    public List<FlushEventListener> getListEventListener()
    {
    		return this.flushEventListeners;
    }
}
