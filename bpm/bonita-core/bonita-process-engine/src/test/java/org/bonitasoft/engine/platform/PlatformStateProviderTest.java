package org.bonitasoft.engine.platform;


import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.platform.PlatformState.STARTED;
import static org.bonitasoft.engine.platform.PlatformState.STARTING;
import static org.bonitasoft.engine.platform.PlatformState.STOPPED;
import static org.bonitasoft.engine.platform.PlatformState.STOPPING;

import org.junit.Test;

public class PlatformStateProviderTest {


    @Test
    public void should_return_false_when_initializingStart_on_STARTING_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STARTING);

        boolean initializeStart = platformStateProvider.initializeStart();

        assertThat(initializeStart).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STARTING);
    }
    @Test
    public void should_return_false_when_initializingStart_on_STOPPING_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STOPPING);

        boolean initializeStart = platformStateProvider.initializeStart();

        assertThat(initializeStart).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STOPPING);
    }

    @Test
    public void should_return_false_when_initializingStart_on_STARTED_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STARTED);

        boolean initializeStart = platformStateProvider.initializeStart();

        assertThat(initializeStart).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STARTED);
    }
    @Test
    public void should_return_true_when_initializingStart_on_STOPPED_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STOPPED);

        boolean initializeStart = platformStateProvider.initializeStart();

        assertThat(initializeStart).isTrue();
        assertThat(platformStateProvider.getState()).isEqualTo(STARTING);
    }

    @Test
    public void should_return_false_when_initializingStop_on_STARTING_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STARTING);

        boolean initializeStop = platformStateProvider.initializeStop();

        assertThat(initializeStop).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STARTING);
    }
    @Test
    public void should_return_false_when_initializingStop_on_STOPPING_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STOPPING);

        boolean initializeStop = platformStateProvider.initializeStop();

        assertThat(initializeStop).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STOPPING);
    }

    @Test
    public void should_return_true_when_initializingStop_on_STARTED_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STARTED);

        boolean initializeStop = platformStateProvider.initializeStop();

        assertThat(initializeStop).isTrue();
        assertThat(platformStateProvider.getState()).isEqualTo(STOPPING);
    }
    @Test
    public void should_return_false_when_initializingStop_on_STOPPED_platform() {
        PlatformStateProvider platformStateProvider = new PlatformStateProvider(STOPPED);

        boolean initializeStop = platformStateProvider.initializeStop();

        assertThat(initializeStop).isFalse();
        assertThat(platformStateProvider.getState()).isEqualTo(STOPPED);
    }
}