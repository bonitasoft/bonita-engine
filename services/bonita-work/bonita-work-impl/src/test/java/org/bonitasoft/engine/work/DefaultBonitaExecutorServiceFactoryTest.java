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

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

public class DefaultBonitaExecutorServiceFactoryTest {

    @Test
    public void ThreadNameInExecutorServiceShouldContainsTenantId() {
        long tenantId = 999;
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(null, tenantId, 1,
                20, 15, 10);

        BonitaExecutorService createExecutorService = defaultBonitaExecutorServiceFactory.createExecutorService();
        Runnable r = new Runnable() {

            @Override
            public void run() {
            }
        };

        String name = ((ThreadPoolExecutor) createExecutorService).getThreadFactory().newThread(r).getName();
        assertThat(name).as("thread name should contains the tenantId").contains(Long.toString(tenantId));
    }
}
