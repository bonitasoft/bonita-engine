/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.platform.StartNodeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.platform.PlatformInfoUpdateScheduledExecutor;
import com.bonitasoft.engine.service.platform.TransactionalPlatformInfoInitializer;
import com.bonitasoft.engine.service.platform.TransactionalPlatformInformationUpdater;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIExtTest {

    @Mock
    private LicenseChecker checker;

    @InjectMocks
    private PlatformAPIExt platformAPI;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = CreationException.class)
    public void createPlatform_should_throw_an_exception_if_the_license_is_invalid() throws Exception {
        when(checker.checkLicense()).thenReturn(false);

        platformAPI.createPlatform();
    }

    @Test
    public void getPlatformLifeCycleServices_should_throws_exception_if_PlatformInfoUpdateScheduledExecutor_is_missing() throws Exception {
        //given
        NodeConfiguration nodeConfiguration = mock(NodeConfiguration.class);
        given(nodeConfiguration.getLifecycleServices()).willReturn(Collections.<PlatformLifecycleService> emptyList());

        //then
        expectedException.expect(StartNodeException.class);
        expectedException.expectMessage("Unable to start node because the mandatory service '" + PlatformInfoUpdateScheduledExecutor.class.getName()
                + "' is not available. The bonita.home content is probably not up to date.");

        //when
        platformAPI.getPlatformServicesToStart(nodeConfiguration);

    }

    @Test
    public void getPlatformLifeCycleServices_should_return_services_from_node_configuration() throws Exception {
        //given
        NodeConfiguration nodeConfiguration = mock(NodeConfiguration.class);
        List<PlatformLifecycleService> services = Arrays.asList(mock(PlatformLifecycleService.class),
                new PlatformInfoUpdateScheduledExecutor(mock(TransactionalPlatformInformationUpdater.class), mock(TransactionalPlatformInfoInitializer.class)),
                mock(PlatformLifecycleService.class));
        given(nodeConfiguration.getLifecycleServices()).willReturn(services);

        //when
        List<PlatformLifecycleService> retrievedServices = platformAPI.getPlatformServicesToStart(nodeConfiguration);

        //then
        assertThat(retrievedServices).isEqualTo(services);

    }

}
