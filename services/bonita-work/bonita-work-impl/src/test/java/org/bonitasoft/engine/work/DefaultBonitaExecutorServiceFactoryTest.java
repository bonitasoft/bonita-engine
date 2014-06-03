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
