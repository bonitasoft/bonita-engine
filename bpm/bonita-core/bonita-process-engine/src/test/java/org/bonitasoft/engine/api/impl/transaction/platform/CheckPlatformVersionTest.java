package org.bonitasoft.engine.api.impl.transaction.platform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckPlatformVersionTest {

    @Mock
    private PlatformService platformService;

    @Mock
    private SPlatform platform;

    @Mock
    private SPlatformProperties properties;

    @Before
    public void initialize() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sameMinorVersion() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("6.1.0");
        when(properties.getPlatformVersion()).thenReturn("6.1.1");

        checkPlatformVersion.execute();
        assertTrue(checkPlatformVersion.sameVersion());
    }

    @Test
    public void sameMaintenanceVersion() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("6.1.1");
        when(properties.getPlatformVersion()).thenReturn("6.1.1-SNAPSHOT");

        checkPlatformVersion.execute();
        assertTrue(checkPlatformVersion.sameVersion());
    }

    @Test
    public void sameVersion() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("6.1.1-SNAPSHOT");
        when(properties.getPlatformVersion()).thenReturn("6.1.1-SNAPSHOT");

        checkPlatformVersion.execute();
        assertTrue(checkPlatformVersion.sameVersion());
    }

    @Test
    public void notSameMinorVersion() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("6.0.3");
        when(properties.getPlatformVersion()).thenReturn("6.1.1-SNAPSHOT");

        checkPlatformVersion.execute();
        assertFalse(checkPlatformVersion.sameVersion());
    }

    @Test
    public void notSameMajorVersion() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("5.0.3");
        when(properties.getPlatformVersion()).thenReturn("6.0.3");

        checkPlatformVersion.execute();
        assertFalse(checkPlatformVersion.sameVersion());
    }

    @Test
    public void notSameMinorVersions() throws SBonitaException {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        when(platformService.getPlatform()).thenReturn(platform);
        when(platformService.getSPlatformProperties()).thenReturn(properties);
        when(platform.getVersion()).thenReturn("6.9");
        when(properties.getPlatformVersion()).thenReturn("6.10");

        checkPlatformVersion.execute();
        assertFalse(checkPlatformVersion.sameVersion());
    }

}
