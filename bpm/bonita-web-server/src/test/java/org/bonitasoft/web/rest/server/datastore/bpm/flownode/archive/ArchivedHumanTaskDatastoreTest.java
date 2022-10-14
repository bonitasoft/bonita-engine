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
import org.bonitasoft.engine.bpm.flownode.impl.internal.ArchivedAutomaticTaskInstanceImpl;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedHumanTaskDatastoreTest {

    ProcessAPI processAPI = mock(ProcessAPI.class);

    @Test
    public void should_thrown_APIItemNotFoundException_when_ArchivedHumanTaskInstance_not_exists() throws Exception {
        APIID id = APIID.makeAPIID(1L);
        ArchivedHumanTaskDatastore abstractArchivedActivityDatastore = spy(
                new ArchivedHumanTaskDatastore(null, ArchivedHumanTaskDefinition.TOKEN));

        doReturn(processAPI).when(abstractArchivedActivityDatastore).getProcessAPI();
        doReturn(new ArchivedAutomaticTaskInstanceImpl("test")).when(processAPI)
                .getArchivedFlowNodeInstance(id.toLong());

        APIItemNotFoundException apiException = assertThrows(APIItemNotFoundException.class,
                () -> abstractArchivedActivityDatastore.get(id));
        assertThat(apiException).isNotNull();
        assertThat(apiException.getMessage()).containsIgnoringCase(ArchivedHumanTaskDefinition.TOKEN);
        assertThat(apiException.getMessage()).containsIgnoringCase(id.toString());
    }

}
