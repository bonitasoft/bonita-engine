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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedCallActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedGatewayInstanceImpl;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedFlowNodeItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchivedActivityDatastoreTest {

    @Mock
    ProcessAPI processAPI;

    @Test
    public void should_return_ArchivedFlowNodeItem() throws Exception {

        APIID id = APIID.makeAPIID(1L);
        ArchivedActivityDatastore abstractArchivedActivityDatastore = spy(
                new ArchivedActivityDatastore(null, ArchivedActivityDefinition.TOKEN));

        doReturn(processAPI).when(abstractArchivedActivityDatastore).getProcessAPI();
        doReturn(new ArchivedCallActivityInstanceImpl("test")).when(processAPI)
                .getArchivedActivityInstance(id.toLong());
        doReturn(new ArchivedActivityItem()).when(abstractArchivedActivityDatastore).convertEngineToConsoleItem(any());

        ArchivedFlowNodeItem archivedFlowNodeItem = abstractArchivedActivityDatastore.get(id);
        assertThat(archivedFlowNodeItem).isNotNull();

    }

    @Test
    public void should_thrown_APIItemNotFoundException_when_ArchiveActivityInstance_not_exists() throws Exception {
        APIID id = APIID.makeAPIID(1L);
        ArchivedActivityDatastore archivedActivityDatastore = spy(
                new ArchivedActivityDatastore(null, ArchivedActivityDefinition.TOKEN));

        doReturn(processAPI).when(archivedActivityDatastore).getProcessAPI();
        doReturn(new ArchivedGatewayInstanceImpl("test")).when(processAPI).getArchivedFlowNodeInstance(id.toLong());

        APIItemNotFoundException apiException = assertThrows(APIItemNotFoundException.class,
                () -> archivedActivityDatastore.get(id));
        assertThat(apiException).isNotNull();
        assertThat(apiException.getMessage()).containsIgnoringCase(ArchivedActivityDefinition.TOKEN);
        assertThat(apiException.getMessage()).containsIgnoringCase(id.toString());
    }

}
