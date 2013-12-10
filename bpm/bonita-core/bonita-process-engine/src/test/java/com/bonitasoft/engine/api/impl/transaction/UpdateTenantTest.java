package com.bonitasoft.engine.api.impl.transaction;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTenantTest {

    @Mock
    private EntityUpdateDescriptor changeDescriptor;

    @Mock
    private PlatformService platformService;

    @Test
    public void testExecute() throws Exception {
        STenant tenant = mock(STenant.class);
        when(platformService.getTenant(241)).thenReturn(tenant);

        new UpdateTenant(241, changeDescriptor, platformService).execute();

        // allows to define in what precise order will the verify orders be executed by Mockito:
        InOrder inOrder = inOrder(platformService);
        inOrder.verify(platformService, times(1)).updateTenant(tenant, changeDescriptor);
        inOrder.verifyNoMoreInteractions();
    }

}
