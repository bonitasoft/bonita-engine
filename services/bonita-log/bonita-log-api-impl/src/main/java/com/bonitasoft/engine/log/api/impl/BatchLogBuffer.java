/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;

/**
 * @author Matthieu Chaffotte
 */
public class BatchLogBuffer {

    private static final BatchLogBuffer buffer = new BatchLogBuffer();

    private final List<SQueriableLog> logs;

    private BatchLogBuffer() {
        this.logs = new ArrayList<SQueriableLog>();
    }

    public static BatchLogBuffer getInstance() {
        return buffer;
    }

    public synchronized void addLogs(final List<SQueriableLog> logs) {
        this.logs.addAll(logs);
    }

    public synchronized List<SQueriableLog> clearLogs() {
    	if (!this.logs.isEmpty()) {
    		final List<SQueriableLog> logsToWrite = new ArrayList<SQueriableLog>(this.logs);
    		this.logs.clear();
    		return logsToWrite;
    	}
    	return Collections.emptyList();
    }

}
