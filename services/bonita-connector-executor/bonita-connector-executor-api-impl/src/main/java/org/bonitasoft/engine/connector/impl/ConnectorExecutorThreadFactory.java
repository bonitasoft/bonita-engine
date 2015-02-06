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
package org.bonitasoft.engine.connector.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baptiste Mesta
 */
public class ConnectorExecutorThreadFactory implements ThreadFactory {

    private static AtomicInteger nbThread = new AtomicInteger(1);

    private final String name;

    public ConnectorExecutorThreadFactory(final String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        return new Thread(runnable, name + "-" + nbThread.getAndIncrement());
    }

}
