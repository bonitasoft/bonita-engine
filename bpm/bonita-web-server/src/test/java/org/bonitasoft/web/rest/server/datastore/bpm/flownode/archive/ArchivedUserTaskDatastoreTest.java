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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedReceiveTaskInstanceImpl;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedUserTaskDatastoreTest {

    @Mock
    ProcessAPI processAPI;

    @Test
    public void should_thrown_APIItemNotFoundException_when_ArchivedUserTaskInstance_not_exists() throws Exception {
        APIID id = APIID.makeAPIID(1L);
        ArchivedUserTaskDatastore abstractArchivedActivityDatastore = spy(
                new ArchivedUserTaskDatastore(null, ArchivedHumanTaskDefinition.TOKEN));

        doReturn(processAPI).when(abstractArchivedActivityDatastore).getProcessAPI();
        doReturn(new ArchivedReceiveTaskInstanceImpl("test")).when(processAPI).getArchivedFlowNodeInstance(id.toLong());

        APIItemNotFoundException apiException = assertThrows(APIItemNotFoundException.class,
                () -> abstractArchivedActivityDatastore.get(id));
        assertThat(apiException).isNotNull();
        assertThat(apiException.getMessage()).containsIgnoringCase(ArchivedHumanTaskDefinition.TOKEN);
        assertThat(apiException.getMessage()).containsIgnoringCase(id.toString());
    }

}
