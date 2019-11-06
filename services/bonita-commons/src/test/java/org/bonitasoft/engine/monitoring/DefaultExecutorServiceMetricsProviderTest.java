package org.bonitasoft.engine.monitoring;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

public class DefaultExecutorServiceMetricsProviderTest {


    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private DefaultExecutorServiceMetricsProvider defaultExecutorServiceMetricsProvider = new DefaultExecutorServiceMetricsProvider();

    @Test
    public void should_register_metrics_when_binding_the_threadpool() {
        defaultExecutorServiceMetricsProvider.bind(meterRegistry, executorService, "my-executor", 14L);

        assertThat(meterRegistry.getMeters()).hasSize(6);
    }

    @Test
    public void should_have_no_more_metrics_when_we_unbind_the_executor() {
        defaultExecutorServiceMetricsProvider.bind(meterRegistry, executorService, "my-executor", 14L);

        defaultExecutorServiceMetricsProvider.unbind(meterRegistry, "my-executor", 14L);

        assertThat(meterRegistry.getMeters()).hasSize(0);
    }

}