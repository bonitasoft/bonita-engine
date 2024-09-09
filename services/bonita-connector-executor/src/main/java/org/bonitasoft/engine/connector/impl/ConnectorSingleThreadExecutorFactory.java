/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.connector.impl;

import static java.util.concurrent.TimeUnit.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.bonitasoft.engine.connector.BonitaConnectorExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(BonitaConnectorExecutorFactory.class)
public class ConnectorSingleThreadExecutorFactory implements BonitaConnectorExecutorFactory {

    private final int queueCapacity;

    public ConnectorSingleThreadExecutorFactory(@Value("${bonita.tenant.connector.queueCapacity}") int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    @Override
    public ThreadPoolExecutor create() {
        return new ThreadPoolExecutor(1, 1, 0L, MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity), new ConnectorExecutorThreadFactory("ConnectorExecutor"),
                new QueueRejectedExecutionHandler());
    }

    public static class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        private static final Logger log = LoggerFactory.getLogger(QueueRejectedExecutionHandler.class);

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            log.warn("The work was rejected. Requeue work : {}", task);
            try {
                executor.getQueue().put(task);
            } catch (final InterruptedException e) {
                throw new RejectedExecutionException("Queuing " + task + " got interrupted.", e);
            }
        }

    }
}
