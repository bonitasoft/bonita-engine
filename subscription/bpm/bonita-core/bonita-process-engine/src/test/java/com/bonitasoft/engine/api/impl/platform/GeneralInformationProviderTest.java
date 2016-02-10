/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.impl.SPlatformImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneralInformationProviderTest {

    @Mock
    private Manager manager;

    @Mock
    private PlatformRetriever platformRetriever;

    @InjectMocks
    @Spy
    private GeneralInformationProvider informationProvider;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        given(informationProvider.getManager()).willReturn(manager);
    }

    @Test
    public void getInfo_should_return_info_from_manager() throws Exception {
        //given
        String platformInfo = "info";
        SPlatformImpl platform = buildPlatform(platformInfo);
        given(platformRetriever.getPlatform()).willReturn(platform);
        doReturn(Collections.singletonMap("key", "value")).when(manager).getInfo(Pair.of("platformInfo", platformInfo));

        //when
        Map<String, String> info = informationProvider.getInfo();

        //then
        assertThat(info).hasSize(1);
        assertThat(info).containsEntry("key", "value");
    }

    @Test
    public void getInfo_should_throw_PlatformNotFoundException_when_platformRetrievers_throws_SPlatformNotFoundException() throws Exception {
        //given
        given(platformRetriever.getPlatform()).willThrow(new SPlatformNotFoundException("msg"));

        //then
        expectedException.expect(PlatformNotFoundException.class);
        expectedException.expectMessage("msg");

        //when
        informationProvider.getInfo();

    }

    @Test
    public void getManager_should_return_instance_of_manager() throws Exception {
        //given
        doCallRealMethod().when(informationProvider).getManager();

        //then
        assertThat(informationProvider.getManager().getClass()).isEqualTo(Manager.class);
    }

    private SPlatformImpl buildPlatform(final String platformInfo) {
        SPlatformImpl platform = new SPlatformImpl("2.0", "1.4", "1.0", "admin", System.currentTimeMillis());
        platform.setInformation(platformInfo);
        return platform;
    }
}