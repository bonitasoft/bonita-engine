/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.core.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.URLAdapterConstants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegacyURLAdapterTest {

    @Mock
    FormMappingService formMappingService;

    @Mock
    ProcessDefinitionService processDefinitionService;

    @Mock
    SProcessDefinition processDefinition;

    @InjectMocks
    LegacyURLAdapter legacyURLAdapter = new LegacyURLAdapter(processDefinitionService, formMappingService);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_generate_legacy_URL_for_process_start_when_mapping_on_legacy() throws Exception {
        String mappingKey = "process/processName/processVersion";
        SFormMappingImpl formMapping = new SFormMappingImpl(1L, FormMappingType.PROCESS_START.getId(), null);
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { "1" });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion%24entry&process=1");
    }

    @Test
    public void should_generate_legacy_URL_for_instance_when_mapping_on_legacy() throws Exception {
        String mappingKey = "processInstance/processName/processVersion";
        SFormMappingImpl formMapping = new SFormMappingImpl(1L, FormMappingType.PROCESS_OVERVIEW.getId(), null);
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);

        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<String, String[]>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { "42" });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion%24recap&instance=42&recap=true");
    }

    @Test
    public void should_generate_legacy_URL_for_task_when_mapping_on_legacy() throws Exception {
        String mappingKey = "process/processName/processVersion";
        SFormMappingImpl formMapping = new SFormMappingImpl(1L, FormMappingType.TASK.getId(), "taskName");
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { "42" });
        queryParametersMap.put(URLAdapterConstants.USER_QUERY_PARAM, new String[] { "2" });
        queryParametersMap.put(URLAdapterConstants.ASSIGN_TASK_QUERY_PARAM, new String[] { "true" });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL)
                .isEqualTo(
                        "/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion--taskName%24entry&task=42&assignTask=true&userId=2");
    }
    
    @Test
    public void should_generate_legacy_URL_for_process_start_when_mapping_on_legacy_with_autInstantiate_param() throws Exception {
        String mappingKey = "process/processName/processVersion";
        SFormMappingImpl formMapping = new SFormMappingImpl(1L, FormMappingType.PROCESS_START.getId(), null);
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { "1" });
        queryParametersMap.put(URLAdapterConstants.AUTO_INSTANTIATE_QUERY_PARAM, new String[] { "false" });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion%24entry&process=1&autoInstantiate=false");
    }
    
    @Test
    public void should_generate_legacy_URL_when_mapping_on_legacy_with_specific_mode() throws Exception {
        String mappingKey = "process/processName/processVersion";
        SFormMappingImpl formMapping = new SFormMappingImpl(1L, FormMappingType.PROCESS_START.getId(), null);
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);

        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] { "1" });
        queryParametersMap.put(URLAdapterConstants.MODE_QUERY_PARAM, new String[] { "app" });
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo(
                "/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=app&form=processName--processVersion%24entry&process=1");
    }

    @Test
    public void adaptShouldThrowIllegalArgumentIf_ID_parameterIsNotPresent() throws SExecutionException {
        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, new HashMap<>());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("parameter \"id\""));

        legacyURLAdapter.adapt(null, null, context);
    }

    @Test
    public void adaptShouldThrowIllegalArgumentIf_ID_parameterHasNoValue() throws SExecutionException {
        Map<String, String[]> queryParametersMap = new HashMap<>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] {});
        Map<String, Serializable> context = new HashMap<>();
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("parameter \"id\""));

        legacyURLAdapter.adapt(null, null, context);
    }

}
