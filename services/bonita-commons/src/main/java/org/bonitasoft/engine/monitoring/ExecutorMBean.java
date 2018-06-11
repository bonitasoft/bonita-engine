/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.engine.monitoring;

public class ExecutorMBean {

    private static double FLUSH_INTERVAL = 10_000;

    private long lastFlush = 0L;
    private long currentNumberOfWorksExecuted;

    private final ObservableExecutor executor;
    private double throughput = 0d;

    public ExecutorMBean(ObservableExecutor executor) {
        this.executor = executor;
    }

    public long getPendings() {
        return executor.getPendings();
    }

    public long getRunnings() {
        return executor.getRunnings();
    }

    public double getThroughput() {
        long now = System.currentTimeMillis();
        if (lastFlush == 0) {
            lastFlush = now;
            currentNumberOfWorksExecuted = executor.getExecuted();
            throughput = 0.0;
        }
        if (lastFlush + FLUSH_INTERVAL <= now) {
            long newNumberOfWorksExecuted = executor.getExecuted();
            throughput = (newNumberOfWorksExecuted - currentNumberOfWorksExecuted) / ((now - lastFlush) / 1000d);
            lastFlush = now;
            currentNumberOfWorksExecuted = newNumberOfWorksExecuted;
        }
        return throughput;
    }

    public long getTotalExecuted() {
        return executor.getExecuted();
    }

}
