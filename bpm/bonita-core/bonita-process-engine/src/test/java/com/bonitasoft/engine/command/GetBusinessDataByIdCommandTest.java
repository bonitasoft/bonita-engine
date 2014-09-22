package com.bonitasoft.engine.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.javacrumbs.jsonunit.assertj.JsonAssert;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.operation.pojo.Travel;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessDataByIdCommandTest {

    @Spy
    private GetBusinessDataByIdCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Before
    public void setUp() throws Exception {
        doReturn(bdrService).when(command).getBusinessDataRepository(tenantServiceAccessor);
    }

    @Test
    public void should_get_the_business_data_based_on_its_identifier() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        final Travel travel = new Travel();
        travel.setNbDays(45);
        when(bdrService.findById(Travel.class, 1983L)).thenReturn(travel);

        final String travelJson = (String) command.execute(parameters, tenantServiceAccessor);
        JsonAssert.assertThatJson(travelJson).isEqualTo("{\"persistenceId\" : 1, \"persistenceVersion\" : 1, \"nbDays\" : 45 }");
    }

    @Test(expected = SCommandParameterizationException.class)
    public void should_throw_exception_when_class_name_does_not_exist() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, "com.bonitasoft.Employee");

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test
    public void should_throw_not_found_exception_when_not_found() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        when(bdrService.findById(Travel.class, 1983L)).thenThrow(new SBusinessDataNotFoundException("not found"));

        try {
            command.execute(parameters, tenantServiceAccessor);
            fail("Business data not found");
        } catch (final SCommandExecutionException scee) {
            assertThat(scee.getCause()).isInstanceOf(DataNotFoundException.class);
        }
    }

}
