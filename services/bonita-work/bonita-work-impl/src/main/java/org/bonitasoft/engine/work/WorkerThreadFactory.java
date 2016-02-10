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
package org.bonitasoft.engine.work;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class WorkerThreadFactory implements ThreadFactory {

    private final AtomicInteger nbThread = new AtomicInteger(1);

    private final String name;

    private final int padding;

    private final long tenantId;

    public WorkerThreadFactory(final String name, final long tenantId, final int maximumPoolSize) {
        this.name = name;
        this.tenantId = tenantId;
        this.padding = guessPadding(maximumPoolSize);
    }

    /**
     * @param maximumPoolSize
     */
    static int guessPadding(final int maximumPoolSize) {
        int tmpPadding = 0;
        int poolSize = maximumPoolSize;
        while (poolSize > 0) {
            poolSize /= 10;
            tmpPadding++;
        }
        return tmpPadding;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append("-");
        builder.append(tenantId);
        builder.append("-");
        builder.append("%0");
        builder.append(padding);
        builder.append("d");
        final String format = String.format(builder.toString(), nbThread.getAndIncrement());
        return new Thread(runnable, format);
    }
}
