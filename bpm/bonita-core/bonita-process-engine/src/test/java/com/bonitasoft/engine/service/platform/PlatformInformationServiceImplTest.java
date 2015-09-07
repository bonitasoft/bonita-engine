/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.impl.SPlatformBuilderFactoryImpl;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformInformationServiceImplTest {

    @Mock
    private PlatformService platformService;

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private PlatformInformationServiceImpl platformInformationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void updatePlatformInfo() throws Exception {
        //given
        SPlatform platform = mock(SPlatform.class);


        //when
        platformInformationService.updatePlatformInfo(platform, "info");

        //then
        ArgumentCaptor<UpdateDescriptor> upDescCaptor = ArgumentCaptor.forClass(UpdateDescriptor.class);
        verify(persistenceService).update(upDescCaptor.capture());
        UpdateDescriptor descriptor = upDescCaptor.getValue();
        assertThat(descriptor.getFields()).containsEntry(new SPlatformBuilderFactoryImpl().getInformationKey(), "info");
        assertThat(descriptor.getEntity()).isEqualTo(platform);
    }

    @Test
    public void updatePlatformInfo_should_throw_exception_when_persistence_service_throws_exception() throws Exception {
        //given
        SPlatform platform = mock(SPlatform.class);
        SPersistenceException toBeThrown = new SPersistenceException("exception");
        doThrow(toBeThrown).when(persistenceService).update(any(UpdateDescriptor.class));

        //then
        expectedException.expect(SPlatformUpdateException.class);
        expectedException.expectCause(new IsEqual<Throwable>(toBeThrown));

        //when
        platformInformationService.updatePlatformInfo(platform, "info");
    }
}