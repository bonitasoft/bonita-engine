/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.monitor.LocalQueueStats;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;

public class HazelcastStatExtractor implements Runnable {

    private static final String EVENT_SERVICE_HANDLERS_PLATFORM = "EVENT_SERVICE_HANDLERS-PLATFORM";

    private static final String SESSION_MAP_PLATFORM = "SESSION_MAP_PLATFORM";

    private static final String EVENT_SERVICE_HANDLERS = "EVENT_SERVICE_HANDLERS-TENANT";

    private static final String SESSION_MAP = "SESSION_MAP";

    private static final String BACKUP_COUNT = " backup count";

    private static final String GET_LATENCY = " get latency";

    private static final String GET_COUNT = " get count";

    private static final String PUT_LATENCY = " put latency";

    private static final String PUT_COUNT = " put count";

    private static final String ITEM_COUNT = " item count";

    private static final String CONNECTOR = "CONNECTOR";

    private static final String EXECUTING_WORK_QUEUE_ITEM_COUNT = "executing work queue item count";

    private static final String EXECUTING_WORK_QUEUE_OFFER_COUNT = "executing work queue offer count";

    private static final String EXECUTING_WORK_QUEUE_POLL_COUNT = "executing work queue poll count";

    private static final String EXECUTING_WORK_QUEUE_REJECTED_OFFER = "executing work queue rejected offer";

    private static final String EXECUTING_WORK_QUEUE_AVG_AGE = "executing work queue avg age";

    private static final String EXECUTING_WORK_QUEUE_MAX_AGE = "executing work queue max age";

    private static final String WORK_QUEUE_ITEM_COUNT = "work queue item count";

    private static final String WORK_QUEUE_OFFER_COUNT = "work queue offer count";

    private static final String WORK_QUEUE_POLL_COUNT = "work queue poll count";

    private static final String WORK_QUEUE_REJECTED_OFFER = "work queue rejected offer";

    private static final String WORK_QUEUE_AVG_AGE = "work queue avg age";

    private static final String WORK_QUEUE_MAX_AGE = "work queue max age";

    private static final String NUMBER_OF_LOCKS = "Number of locks";

    private final HazelcastInstance hazelcastInstance;

    private final long statsPrintInterval;

    private final FileWriter fileWriter;

    private final ArrayList<String> statsList;

    public HazelcastStatExtractor(final HazelcastInstance hazelcastInstance, final long statsPrintInterval) throws IOException, BonitaHomeNotSetException {
        this.hazelcastInstance = hazelcastInstance;
        this.statsPrintInterval = statsPrintInterval;
        final String fileName = "hazelcast-stats-" + hazelcastInstance.getCluster().getLocalMember().getUuid() + ".csv";

        final File file = BonitaHomeServer.getInstance().getPlatformTempFile(fileName);
        fileWriter = new FileWriter(file);

        System.out.println("Saving hazelcast statistics in " + file);

        statsList = new ArrayList<String>();
        statsList.add(EXECUTING_WORK_QUEUE_ITEM_COUNT);
        statsList.add(EXECUTING_WORK_QUEUE_OFFER_COUNT);
        statsList.add(EXECUTING_WORK_QUEUE_POLL_COUNT);
        statsList.add(EXECUTING_WORK_QUEUE_REJECTED_OFFER);
        statsList.add(EXECUTING_WORK_QUEUE_AVG_AGE);
        statsList.add(EXECUTING_WORK_QUEUE_MAX_AGE);
        statsList.add(WORK_QUEUE_ITEM_COUNT);
        statsList.add(WORK_QUEUE_OFFER_COUNT);
        statsList.add(WORK_QUEUE_POLL_COUNT);
        statsList.add(WORK_QUEUE_REJECTED_OFFER);
        statsList.add(WORK_QUEUE_AVG_AGE);
        statsList.add(WORK_QUEUE_MAX_AGE);
        statsList.add(NUMBER_OF_LOCKS);
        addMap(statsList, CONNECTOR);
        addMap(statsList, SESSION_MAP);
        addMap(statsList, SESSION_MAP_PLATFORM);
        addMap(statsList, EVENT_SERVICE_HANDLERS);
        addMap(statsList, EVENT_SERVICE_HANDLERS_PLATFORM);

    }

    private void addMap(final ArrayList<String> statsList, final String mapName) {
        statsList.add(mapName + ITEM_COUNT);
        statsList.add(mapName + GET_COUNT);
        statsList.add(mapName + GET_LATENCY);
        statsList.add(mapName + PUT_COUNT);
        statsList.add(mapName + PUT_LATENCY);
        statsList.add(mapName + BACKUP_COUNT);
    }

    @Override
    public void run() {
        ArrayList<String> header = new ArrayList<String>(statsList);
        header.add(0, "Time");
        CSVWriter csvWriter = new CSVWriter(fileWriter, header.toArray(new String[] {}));
        while (true) {
            try {
                Thread.sleep(statsPrintInterval);
            } catch (InterruptedException e) {
                break;
            }
            Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
            long nblock = 0;
            HashMap<String, Long> stats = new HashMap<String, Long>();
            for (DistributedObject distributedObject : distributedObjects) {
                if (distributedObject instanceof ILock) {
                    nblock++;
                }
                if (distributedObject instanceof IQueue) {
                    IQueue<?> queue = (IQueue<?>) distributedObject;
                    if (queue.getName().startsWith("ExecutingWorkQueue")) {
                        LocalQueueStats localQueueStats = queue.getLocalQueueStats();
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_ITEM_COUNT, localQueueStats.getOwnedItemCount());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_ITEM_COUNT, localQueueStats.getOwnedItemCount());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_OFFER_COUNT, localQueueStats.getOfferOperationCount());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_POLL_COUNT, localQueueStats.getPollOperationCount());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_AVG_AGE, localQueueStats.getAvgAge());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_MAX_AGE, localQueueStats.getMaxAge());
                        putOrAdd(stats, EXECUTING_WORK_QUEUE_REJECTED_OFFER, localQueueStats.getRejectedOfferOperationCount());
                    } else if (queue.getName().startsWith("WorkQueue")) {
                        LocalQueueStats localQueueStats = queue.getLocalQueueStats();
                        putOrAdd(stats, WORK_QUEUE_ITEM_COUNT, localQueueStats.getOwnedItemCount());
                        putOrAdd(stats, WORK_QUEUE_OFFER_COUNT, localQueueStats.getOfferOperationCount());
                        putOrAdd(stats, WORK_QUEUE_POLL_COUNT, localQueueStats.getPollOperationCount());
                        putOrAdd(stats, WORK_QUEUE_AVG_AGE, localQueueStats.getAvgAge());
                        putOrAdd(stats, WORK_QUEUE_MAX_AGE, localQueueStats.getMaxAge());
                        putOrAdd(stats, WORK_QUEUE_REJECTED_OFFER, localQueueStats.getRejectedOfferOperationCount());
                    }
                }
                if (distributedObject instanceof IMap) {
                    IMap<?, ?> map = (IMap<?, ?>) distributedObject;
                    if (map.getName().endsWith(CONNECTOR)) {
                        extractMapStats(stats, map, CONNECTOR);
                    }
                    if (map.getName().equals(SESSION_MAP)) {
                        extractMapStats(stats, map, SESSION_MAP);
                    }
                    if (map.getName().equals(SESSION_MAP_PLATFORM)) {
                        extractMapStats(stats, map, SESSION_MAP_PLATFORM);
                    }
                    if (map.getName().startsWith(EVENT_SERVICE_HANDLERS)) {
                        extractMapStats(stats, map, EVENT_SERVICE_HANDLERS);
                    }
                    if (map.getName().startsWith(EVENT_SERVICE_HANDLERS_PLATFORM)) {
                        extractMapStats(stats, map, EVENT_SERVICE_HANDLERS_PLATFORM);
                    }
                }
            }
            stats.put(NUMBER_OF_LOCKS, nblock);
            ArrayList<String> counts = new ArrayList<String>();
            counts.add(String.valueOf(System.currentTimeMillis()));
            for (String name : statsList) {
                Long count = stats.get(name);
                counts.add(String.valueOf(count));
            }

            String[] array = counts.toArray(new String[] {});
            csvWriter.writeNext(array);
        }
        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * put the element or add it if it already exists (must do that because there may be more than one work queue)
     * 
     * @param stats
     * @param executingWorkQueueItemCount
     * @param ownedItemCount
     */
    private void putOrAdd(final HashMap<String, Long> stats, final String key, final long value) {
        long sum = value;
        if (stats.containsKey(key)) {
            sum += stats.get(key);
        }
        stats.put(key, sum);
    }

    private void extractMapStats(final HashMap<String, Long> stats, final IMap<?, ?> map, final String mapName) {
        LocalMapStats localMapStats = map.getLocalMapStats();
        stats.put(mapName + ITEM_COUNT, localMapStats.getOwnedEntryCount());
        long getCount = localMapStats.getGetOperationCount();
        stats.put(mapName + GET_COUNT, getCount);
        getCount = Math.max(1, getCount);
        stats.put(mapName + GET_LATENCY, localMapStats.getTotalGetLatency() / getCount);
        long putCount = localMapStats.getPutOperationCount();
        putCount = Math.max(1, putCount);
        stats.put(mapName + PUT_COUNT, putCount);
        stats.put(mapName + PUT_LATENCY, localMapStats.getTotalPutLatency() / putCount);
        stats.put(mapName + BACKUP_COUNT, localMapStats.getBackupEntryCount());
    }
}
