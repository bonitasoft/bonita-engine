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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APIActorTest {

    @Mock
    private ProcessDatastore processDatastore;

    private APIActor apiActor;

    @BeforeAll
    static void initEnvironment() {
        I18n.getInstance();
    }

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiActor = spy(new APIActor());
    }

    @Test
    void fillDeploys_should_fill_process_when_process_is_active() {
        // Given an actor whose process definition still exists
        doReturn(processDatastore).when(apiActor).getProcessDatastore();
        final APIID processId = APIID.makeAPIID(7L);
        final ActorItem item = mock(ActorItem.class);
        doReturn("7").when(item).getAttributeValue(ActorItem.ATTRIBUTE_PROCESS_ID);
        doReturn(processId).when(item).getProcessId();

        final List<String> deploys = List.of(ActorItem.ATTRIBUTE_PROCESS_ID);

        final ProcessItem processItem = new ProcessItem();
        doReturn(processItem).when(processDatastore).get(processId);

        // When
        apiActor.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(ActorItem.ATTRIBUTE_PROCESS_ID, processItem);
    }

    @Test
    void fillDeploys_should_skip_process_deploy_when_process_no_longer_exists() {
        // Given an actor whose process definition was deleted
        doReturn(processDatastore).when(apiActor).getProcessDatastore();
        final APIID deletedProcessId = APIID.makeAPIID(7L);
        final ActorItem item = mock(ActorItem.class);
        doReturn("7").when(item).getAttributeValue(ActorItem.ATTRIBUTE_PROCESS_ID);
        doReturn(deletedProcessId).when(item).getProcessId();

        final List<String> deploys = List.of(ActorItem.ATTRIBUTE_PROCESS_ID);

        doThrow(new APIItemNotFoundException("process", deletedProcessId,
                new ProcessDefinitionNotFoundException("process deleted")))
                        .when(processDatastore).get(deletedProcessId);

        // When the unresolvable process must not fail the whole actor list
        assertThatCode(() -> apiActor.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the process deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(ActorItem.ATTRIBUTE_PROCESS_ID), any(Item.class));
    }
}
