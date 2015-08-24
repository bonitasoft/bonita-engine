/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.impl.SPlatformImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformInfoInitializerTest {

    @Mock
    private PlatformInformationService platformInformationService;

    @Mock
    private PlatformRetriever platformRetriever;

    @Mock
    private Manager manager;

    @InjectMocks
    private PlatformInfoInitializer platformInfoInitializer;

    @Test
    public void ensurePlatformInfoIsSet_should_set_platform_info_when_it_is_null() throws Exception {
        //given
        SPlatformImpl platform = buildPlatform(null);
        given(platformRetriever.getPlatform()).willReturn(platform);
        given(manager.calculateNewPlatformInfo(null)).willReturn("firstInfo");

        //when
        platformInfoInitializer.ensurePlatformInfoIsSet();

        //then
        verify(platformInformationService).updatePlatformInfo(platform, "firstInfo");
    }

    @Test
    public void ensurePlatformInfoIsSet_should_not_update_platform_info_when_it_is_set() throws Exception {
        //given
        SPlatformImpl platform = buildPlatform("someValue");
        given(platformRetriever.getPlatform()).willReturn(platform);

        //when
        platformInfoInitializer.ensurePlatformInfoIsSet();

        //then
        verifyZeroInteractions(manager);
        verify(platformInformationService, never()).updatePlatformInfo(any(SPlatform.class), anyString());
    }

    private SPlatformImpl buildPlatform(final String information) {
        SPlatformImpl platform = new SPlatformImpl("2.0", "1.4", "1.0", "user", System.currentTimeMillis());
        platform.setInformation(information);
        return platform;
    }
}