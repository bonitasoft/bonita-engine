/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.log.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;

/**
 * @author Matthieu Chaffotte
 */
public class BatchLogBuffer {

    private static final BatchLogBuffer buffer = new BatchLogBuffer();

    private final List<SQueriableLog> logs;

    private BatchLogBuffer() {
        logs = new ArrayList<SQueriableLog>();
    }

    public static BatchLogBuffer getInstance() {
        return buffer;
    }

    public synchronized void addLogs(final List<SQueriableLog> logs) {
        logs.addAll(logs);
    }

    public synchronized List<SQueriableLog> clearLogs() {
        final List<SQueriableLog> logsToWrite = new ArrayList<SQueriableLog>(logs);
        logs.clear();
        return logsToWrite;
    }

}
