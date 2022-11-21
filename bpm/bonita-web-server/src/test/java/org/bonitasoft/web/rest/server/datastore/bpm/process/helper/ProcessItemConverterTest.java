/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.web.rest.server.datastore.bpm.process.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.TenantCacheUtilFactory;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessDeploymentInfoImpl;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.CommonDateFormater;
import org.bonitasoft.web.toolkit.server.utils.ServerDateFormater;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessItemConverterTest {

    private ProcessItemConverter processItemConverter;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private ActorInstance actorInstance1;

    @Mock
    private ActorInstance actorInstance2;

    @Before
    public void setUp() throws Exception {
        I18n.getInstance();
        CommonDateFormater.setDateFormater(new ServerDateFormater());
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        processItemConverter = spy(new ProcessItemConverter(processAPI));
        TenantCacheUtilFactory.clearTenantCacheUtil();
    }

    @Test
    public void shouldReadActorInitiatorFromCacheOnSecondCall()
            throws ProcessDefinitionNotFoundException, ActorNotFoundException {

        when(processAPI.getActorInitiator(3L)).thenReturn(actorInstance2);
        doReturn(6L).when(actorInstance2).getId();

        ProcessDeploymentInfo processDeploymentInfo = new ProcessDeploymentInfoImpl(1, 3L, "ProcessName", "Version",
                "Description", new Date(), 3, ActivationState.ENABLED, ConfigurationState.RESOLVED, "displayName",
                new Date(), "iconPath",
                "displayDescription");

        ProcessItem processItem = processItemConverter.convert(processDeploymentInfo);

        //Get 2 ActorInitiatorId from engine then store them in cache
        assertEquals("6", processItem.getActorInitiatorId());

        processItem = processItemConverter.convert(processDeploymentInfo);

        //Get  ActorInitiatorId from cache
        assertEquals("6", processItem.getActorInitiatorId());
        //it should call getActorInitiator only one times because the second should be read from the cache
        verify(processAPI, times(1)).getActorInitiator(3L);
    }

    @Test
    public void shouldStoreDifferentActorInitiatorIntoCache()
            throws ActorNotFoundException, ProcessDefinitionNotFoundException {

        when(processAPI.getActorInitiator(1L)).thenReturn(actorInstance1);
        when(processAPI.getActorInitiator(2L)).thenReturn(actorInstance2);
        doReturn(5L).when(actorInstance1).getId();
        doReturn(6L).when(actorInstance2).getId();

        ProcessDeploymentInfo firstProcessDeploymentInfo = new ProcessDeploymentInfoImpl(1, 1, "ProcessName1",
                "Version1",
                "Description1", new Date(), 3, ActivationState.ENABLED, ConfigurationState.RESOLVED, "displayName1",
                new Date(), "iconPath1",
                "displayDescription1");
        ProcessDeploymentInfo secondProcessDeploymentInfo = new ProcessDeploymentInfoImpl(2, 2, "ProcessName2",
                "Version2",
                "Description2", new Date(), 3, ActivationState.ENABLED, ConfigurationState.RESOLVED, "displayName2",
                new Date(), "iconPath2",
                "displayDescription2");

        //Get 2 ActorInitiatorId from engine then store them in cache
        ProcessItem processItem = processItemConverter.convert(firstProcessDeploymentInfo);
        assertEquals("5", processItem.getActorInitiatorId());
        processItem = processItemConverter.convert(secondProcessDeploymentInfo);
        assertEquals("6", processItem.getActorInitiatorId());

        // Read 2 different ActorInitiatorId from cache
        processItem = processItemConverter.convert(firstProcessDeploymentInfo);
        assertEquals("5", processItem.getActorInitiatorId());
        processItem = processItemConverter.convert(secondProcessDeploymentInfo);
        assertEquals("6", processItem.getActorInitiatorId());

        //it should call getActorInitiator only one times because the second should be read from the cache
        verify(processAPI, times(1)).getActorInitiator(1L);
        verify(processAPI, times(1)).getActorInitiator(2L);
    }

}
