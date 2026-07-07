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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Nicolas TITH
 */
@ExtendWith(MockitoExtension.class)
class APIArchivedCaseTest {

    @Mock
    private UserDatastore userDatastore;

    @Mock
    private ProcessDatastore processDatastore;

    @Mock
    private ArchivedCaseDatastore archivedCaseDatastore;

    private APIArchivedCase apiArchivedCase;

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiArchivedCase = spy(new APIArchivedCase());
    }

    @Test
    void delete_should_delete_several_items() {
        // Given
        doReturn(archivedCaseDatastore).when(apiArchivedCase).defineDefaultDatastore();
        final List<APIID> idList = Arrays.asList(APIID.makeAPIID(1L), APIID.makeAPIID(2L), APIID.makeAPIID(3L));

        // When
        apiArchivedCase.delete(idList);

        // Then
        verify(archivedCaseDatastore).delete(idList);
    }

    @Test
    void fillDeploys_should_fill_user_who_started_case_when_deploy_of_started_by_is_active() {
        // Given
        doReturn(userDatastore).when(apiArchivedCase).getUserDatastore();
        final APIID startedByUserId = APIID.makeAPIID(3L);
        final ArchivedCaseItem item = mock(ArchivedCaseItem.class);
        doReturn(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(startedByUserId).when(item).getStartedByUserId();

        final List<String> deploys = List.of(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(startedByUserId);

        // When
        apiArchivedCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_skip_started_by_deploy_and_keep_other_deploys_when_user_no_longer_exists() {
        // Given an archived case started by a user that was deleted from the organization, plus a still-existing process
        doReturn(userDatastore).when(apiArchivedCase).getUserDatastore();
        doReturn(processDatastore).when(apiArchivedCase).getProcessDatastore();

        final APIID deletedUserId = APIID.makeAPIID(3L);
        final APIID processId = APIID.makeAPIID(9L);
        final ArchivedCaseItem item = mock(ArchivedCaseItem.class);
        // lenient: fillDeploys also probes startedBySubstitute (once the fix lets it continue past the
        // unresolvable started_by deploy) while the processDefinitionId stub is still unused
        lenient().doReturn(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(deletedUserId).when(item).getStartedByUserId();
        lenient().doReturn(ArchivedCaseItem.ATTRIBUTE_PROCESS_ID).when(item)
                .getAttributeValue(ArchivedCaseItem.ATTRIBUTE_PROCESS_ID);
        doReturn(processId).when(item).getProcessId();

        final List<String> deploys = Arrays.asList(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                ArchivedCaseItem.ATTRIBUTE_PROCESS_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);
        final ProcessItem processItem = new ProcessItem();
        doReturn(processItem).when(processDatastore).get(processId);

        // When the unresolvable user deploy must not fail the whole request
        assertThatCode(() -> apiArchivedCase.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the started_by deploy is skipped, but the process deploy still succeeds
        verify(item, never()).setDeploy(eq(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID), any());
        verify(item).setDeploy(ArchivedCaseItem.ATTRIBUTE_PROCESS_ID, processItem);
    }

    @Test
    void fillDeploys_should_skip_started_by_substitute_deploy_when_user_no_longer_exists() {
        // Given an archived case whose substitute starter was deleted from the organization
        doReturn(userDatastore).when(apiArchivedCase).getUserDatastore();

        final APIID deletedUserId = APIID.makeAPIID(6L);
        final ArchivedCaseItem item = mock(ArchivedCaseItem.class);
        // lenient: fillDeploys probes started_by first, before this startedBySubstitute stub is matched
        lenient().doReturn(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID).when(item)
                .getAttributeValue(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);
        doReturn(deletedUserId).when(item).getStartedBySubstituteUserId();

        final List<String> deploys = List.of(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When / Then the unresolvable substitute deploy is skipped without failing
        assertThatCode(() -> apiArchivedCase.fillDeploys(item, deploys)).doesNotThrowAnyException();
        verify(item, never()).setDeploy(eq(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID), any());
    }
}
