/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 */
public class WorkerThreadFactory implements ThreadFactory {

    private final AtomicInteger nbThread = new AtomicInteger(1);

    private final String name;

    private final int padding;

    public WorkerThreadFactory(final String name, final int maximumPoolSize) {
        this.name = name;
        this.padding = guessPadding(maximumPoolSize);
    }

    /**
     * @param maximumPoolSize
     */
    static int guessPadding(int maximumPoolSize) {
        int tmpPadding = 0;
        while (maximumPoolSize > 0) {
            maximumPoolSize /= 10;
            tmpPadding++;
        }
        return tmpPadding;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        return new Thread(runnable, String.format(name + "-" + "%0" + padding + "d", nbThread.getAndIncrement()));
    }

}
