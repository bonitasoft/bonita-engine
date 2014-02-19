package org.bonitasoft.engine.work;

import static org.fest.assertions.Assertions.assertThat;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;

public class DefaultBonitaExecutorServiceFactoryTest {

    @Test
    public void ThreadNameInExecutorServiceShouldContainsTenantId() throws Exception {
        long tenantId = 999;
        DefaultBonitaExecutorServiceFactory defaultBonitaExecutorServiceFactory = new DefaultBonitaExecutorServiceFactory(tenantId, 1,
                20, 15, (long) 10);

        ThreadPoolExecutor createExecutorService = defaultBonitaExecutorServiceFactory.createExecutorService();
        Runnable r = new Runnable() {

            @Override
            public void run() {
            } 
        };

        String name = createExecutorService.getThreadFactory().newThread(r).getName();
        assertThat(name).as("thread name should contains the tenantId").contains(Long.toString(tenantId));
    }
}
