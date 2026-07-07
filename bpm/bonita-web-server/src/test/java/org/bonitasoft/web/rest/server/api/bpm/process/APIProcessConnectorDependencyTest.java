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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem;
import org.bonitasoft.web.rest.server.BonitaRestAPIServlet;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Colin PUY
 */
@ExtendWith(MockitoExtension.class)
class APIProcessConnectorDependencyTest {

    @Spy
    private APIProcessConnectorDependency apiProcessConnectorDependency;

    @BeforeAll
    static void initEnvironment() {
        new BonitaRestAPIServlet();
        I18n.getInstance();
    }

    private Map<String, String> buildFilters(String processId, String connectorName, String connectorVersion) {
        Map<String, String> filters = new HashMap<>();
        filters.put(ATTRIBUTE_PROCESS_ID, processId);
        filters.put(ATTRIBUTE_CONNECTOR_NAME, connectorName);
        filters.put(ATTRIBUTE_CONNECTOR_VERSION, connectorVersion);
        return filters;
    }

    @Test
    void checkMandatoryAttributes_should_require_process_id() {
        Map<String, String> filters = buildFilters(null, "aConnectorName", "aConnectorVersion");

        assertThatExceptionOfType(APIFilterMandatoryException.class)
                .isThrownBy(() -> apiProcessConnectorDependency.checkMandatoryAttributes(filters));
    }

    @Test
    void checkMandatoryAttributes_should_require_connector_name() {
        Map<String, String> filters = buildFilters("1", "", "aConnectorVersion");

        assertThatExceptionOfType(APIFilterMandatoryException.class)
                .isThrownBy(() -> apiProcessConnectorDependency.checkMandatoryAttributes(filters));
    }

    @Test
    void checkMandatoryAttributes_should_require_connector_version() {
        Map<String, String> filters = buildFilters("1", "aConnectorName", "");

        assertThatExceptionOfType(APIFilterMandatoryException.class)
                .isThrownBy(() -> apiProcessConnectorDependency.checkMandatoryAttributes(filters));
    }

    @Test
    void checkMandatoryAttributes_should_not_throw_when_all_attributes_are_set() {
        Map<String, String> filters = buildFilters("1", "aConnectorName", "aConnectorVersion");

        assertThatCode(() -> apiProcessConnectorDependency.checkMandatoryAttributes(filters))
                .doesNotThrowAnyException();
    }

    @Test
    void search_should_check_mandatory_attributes() {
        Map<String, String> filtersWithoutProcessId = buildFilters(null, "aConnectorName", "aConnectorVersion");

        assertThatExceptionOfType(APIFilterMandatoryException.class)
                .isThrownBy(() -> apiProcessConnectorDependency.search(0, 10, null, null, filtersWithoutProcessId));
    }

    @Test
    void fillDeploys_should_skip_process_deploy_when_process_no_longer_exists() {
        // Given a connector dependency whose process definition was deleted
        final ProcessDatastore processDatastore = mock(ProcessDatastore.class);
        doReturn(processDatastore).when(apiProcessConnectorDependency).getProcessDatastore();
        final APIID deletedProcessId = APIID.makeAPIID(7L);
        final ProcessConnectorDependencyItem item = mock(ProcessConnectorDependencyItem.class);
        doReturn("7").when(item).getAttributeValue(ATTRIBUTE_PROCESS_ID);
        doReturn(deletedProcessId).when(item).getProcessId();

        final List<String> deploys = List.of(ATTRIBUTE_PROCESS_ID);

        doThrow(new APIItemNotFoundException("process", deletedProcessId,
                new ProcessDefinitionNotFoundException("process deleted")))
                        .when(processDatastore).get(deletedProcessId);

        // When the unresolvable process must not fail the whole dependency list
        assertThatCode(() -> apiProcessConnectorDependency.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the process deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(ATTRIBUTE_PROCESS_ID), any(Item.class));
    }
}
