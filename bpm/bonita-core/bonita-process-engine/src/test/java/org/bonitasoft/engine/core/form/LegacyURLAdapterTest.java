package org.bonitasoft.engine.core.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.URLAdapterConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegacyURLAdapterTest {

	@Mock
	FormMappingService formMappingService;
	
	@Mock
	ProcessDefinitionService processDefinitionService;
	
	@Mock
	SFormMapping formMapping;
	
	@Mock
	SProcessDefinition processDefinition;
	
	@Spy
	@InjectMocks
	LegacyURLAdapter legacyURLAdapter = new LegacyURLAdapter(processDefinitionService, formMappingService);

    @Test
    public void should_generate_legacy_URL_for_process_when_mapping_on_legacy() throws Exception {
    	String mappingKey = "process/processName/processVersion";
    	when(formMapping.getProcessDefinitionId()).thenReturn(1L);
    	when(formMapping.getTask()).thenReturn(null);
    	when(formMapping.getType()).thenReturn(FormMappingType.PROCESS_START.getId());
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);
        
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<String, String[]>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] {"1"});
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);
        
		String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo("/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion%24entry&process=1&autoInstantiate=false");
    }

    @Test
    public void should_generate_legacy_URL_for_instance_when_mapping_on_legacy() throws Exception {
    	String mappingKey = "processInstance/processName/processVersion";
    	when(formMapping.getProcessDefinitionId()).thenReturn(1L);
    	when(formMapping.getTask()).thenReturn(null);
    	when(formMapping.getType()).thenReturn(FormMappingType.PROCESS_OVERVIEW.getId());
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);
        
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<String, String[]>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] {"42"});
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);
        
		String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo("/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion%24recap&instance=42&recap=true");
    }

    @Test
    public void should_generate_legacy_URL_for_task_when_mapping_on_legacy() throws Exception {
    	String mappingKey = "process/processName/processVersion";
    	when(formMapping.getProcessDefinitionId()).thenReturn(1L);
    	when(formMapping.getTask()).thenReturn("taskName");
    	when(formMapping.getType()).thenReturn(FormMappingType.TASK.getId());
        when(formMappingService.get(mappingKey)).thenReturn(formMapping);
        when(processDefinition.getName()).thenReturn("processName");
        when(processDefinition.getVersion()).thenReturn("processVersion");
        when(processDefinitionService.getProcessDefinition(1L)).thenReturn(processDefinition);
        
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put(URLAdapterConstants.CONTEXT_PATH, "/bonita");
        context.put(URLAdapterConstants.LOCALE, "en");
        Map<String, String[]> queryParametersMap = new HashMap<String, String[]>();
        queryParametersMap.put(URLAdapterConstants.ID_QUERY_PARAM, new String[] {"42"});
        queryParametersMap.put(URLAdapterConstants.USER_QUERY_PARAM, new String[] {"2"});
        context.put(URLAdapterConstants.QUERY_PARAMETERS, (Serializable) queryParametersMap);
        
		String legacyURL = legacyURLAdapter.adapt(null, mappingKey, context);

        assertThat(legacyURL).isEqualTo("/bonita/portal/homepage?ui=form&locale=en&theme=1#mode=form&form=processName--processVersion--taskName%24entry&task=42&userId=2");
    }
}
