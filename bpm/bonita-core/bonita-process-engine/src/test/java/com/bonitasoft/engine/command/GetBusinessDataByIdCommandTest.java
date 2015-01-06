/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.javacrumbs.jsonunit.assertj.JsonAssert;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.operation.pojo.Travel;
import com.bonitasoft.engine.pojo.Address;
import com.bonitasoft.engine.pojo.AddressBook;
import com.bonitasoft.engine.pojo.Client;
import com.bonitasoft.engine.pojo.Command;
import com.bonitasoft.engine.pojo.CommandLine;
import com.bonitasoft.engine.pojo.Country;
import com.bonitasoft.engine.pojo.NameList;
import com.bonitasoft.engine.pojo.Product;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessDataByIdCommandTest {

    private static final String BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    private GetBusinessDataByIdCommand command;

    @Mock
    private BusinessDataRepository bdrService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Before
    public void setUp() throws Exception {
        command = new GetBusinessDataByIdCommand();
        when(tenantServiceAccessor.getBusinessDataRepository()).thenReturn(bdrService);
    }

    @Test
    public void should_get_the_business_data_based_on_its_identifier() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        final Travel travel = new Travel();
        travel.setNbDays(45);
        travel.setPersistenceId(1L);
        travel.setPersistenceVersion(1L);
        when(bdrService.findById(Travel.class, 1983L)).thenReturn(travel);

        final String travelJson = (String) command.execute(parameters, tenantServiceAccessor);
        JsonAssert.assertThatJson(travelJson).isEqualTo("{\"persistenceId\" : 1, \"persistenceVersion\" : 1, \"nbDays\" : 45 }");
    }

    @Test(expected = SCommandParameterizationException.class)
    public void should_throw_exception_when_class_name_does_not_exist() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, "com.bonitasoft.Employee");
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test
    public void should_throw_not_found_exception_when_not_found() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        when(bdrService.findById(Travel.class, 1983L)).thenThrow(new SBusinessDataNotFoundException("not found"));

        try {
            command.execute(parameters, tenantServiceAccessor);
            fail("Business data not found");
        } catch (final SCommandExecutionException scee) {
            assertThat(scee.getCause()).isInstanceOf(DataNotFoundException.class);
        }
    }

    @Test
    public void should_get_the_business_data_in_lazymode_based_on_its_identifier() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, AddressBook.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        final AddressBook addressBook = new AddressBook();
        when(bdrService.findById(AddressBook.class, 1983L)).thenReturn(addressBook);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);

        final String expectedJson = IOUtils.toString(GetBusinessDataByIdCommandTest.class.getResource("/rest/getProxyAddressBook.json"));
        JsonAssert.assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test
    public void execute_should_return_the_list_child_of_an_entity() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Command.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "lines");
        final Product pencil = new Product(687L, 1L, "Pencil");
        final Product paper = new Product(688L, 12L, "Paper");
        final Command command1 = new Command(1983L, 0L);
        command1.addLine(new CommandLine(864L, 78L, pencil, 5));
        command1.addLine(new CommandLine(964L, 7L, paper, 10));
        when(bdrService.findById(Command.class, 1983L)).thenReturn(command1);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);

        final String expectedJson = IOUtils.toString(GetBusinessDataByIdCommandTest.class.getResource("/rest/listOfCommandLines.json"));
        JsonAssert.assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test
    public void execute_should_return_the_child_of_an_entity() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, CommandLine.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "product");
        final Product pencil = new Product(687L, 1L, "Pencil");
        final CommandLine commandLine = new CommandLine(864L, 78L, pencil, 5);
        when(bdrService.findById(CommandLine.class, 864L)).thenReturn(commandLine);
        when(bdrService.unwrap(pencil)).thenReturn(pencil);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);

        final String expectedJson = IOUtils.toString(GetBusinessDataByIdCommandTest.class.getResource("/rest/getProductOfCommandLine.json"));
        JsonAssert.assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test
    public void execute_should_return_the_entity_if_child_name_is_empty() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, CommandLine.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "");
        final Product pencil = new Product(687L, 1L, "Pencil");
        final CommandLine commandLine = new CommandLine(864L, 78L, pencil, 5);
        when(bdrService.findById(CommandLine.class, 864L)).thenReturn(commandLine);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);

        final String expectedJson = IOUtils.toString(GetBusinessDataByIdCommandTest.class.getResource("/rest/getCommandLine.json"));
        JsonAssert.assertThatJson(json).isEqualTo(expectedJson);
    }

    @Test(expected = SCommandExecutionException.class)
    public void execute_should_throw_an_exception_if_field_does_not_exist() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, CommandLine.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "address");
        final Product pencil = new Product(687L, 1L, "Pencil");
        final CommandLine commandLine = new CommandLine(864L, 78L, pencil, 5);
        when(bdrService.findById(CommandLine.class, 864L)).thenReturn(commandLine);

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandExecutionException.class)
    public void execute_should_throw_an_exception_if_field_is_not_an_entity_or_a_list_of_entity() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, CommandLine.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "persistenceVersion");
        final Product pencil = new Product(687L, 1L, "Pencil");
        final CommandLine commandLine = new CommandLine(864L, 78L, pencil, 5);
        when(bdrService.findById(CommandLine.class, 864L)).thenReturn(commandLine);

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandExecutionException.class)
    public void execute_should_throw_an_exception_if_a_parse_exception_occurs_during_serialization() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        final GetBusinessDataByIdCommand command = new GetBusinessDataByIdCommand(mapper);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        final Travel travel = new Travel();
        travel.setNbDays(45);
        when(bdrService.findById(Travel.class, 1983L)).thenReturn(travel);
        doThrow(JsonProcessingException.class).when(mapper).writeValue(any(Writer.class), any(JsonNode.class));

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandExecutionException.class)
    public void execute_should_throw_an_exception_if_an_io_exception_occurs_during_serialization() throws Exception {
        final ObjectMapper mapper = mock(ObjectMapper.class);
        final GetBusinessDataByIdCommand command = new GetBusinessDataByIdCommand(mapper);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Travel.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        final Travel travel = new Travel();
        travel.setNbDays(45);
        when(bdrService.findById(Travel.class, 1983L)).thenReturn(travel);
        doThrow(IOException.class).when(mapper).writeValue(any(Writer.class), any(JsonNode.class));

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test(expected = SCommandExecutionException.class)
    public void execute_should_throw_an_exception_if_field_is_a_list_but_not_of_entity() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, NameList.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "names");
        final NameList nameList = new NameList();
        nameList.setPersistenceId(864L);
        nameList.setNames(Arrays.asList("Matti", "Akseli"));
        when(bdrService.findById(NameList.class, 864L)).thenReturn(nameList);

        command.execute(parameters, tenantServiceAccessor);
    }

    @Test
    public void should_return_empty_json_object_when_child_is_null() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 864L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, CommandLine.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_CHILD_NAME, "product");
        final CommandLine commandLine = new CommandLine(864L, 78L, null, 5);
        when(bdrService.findById(CommandLine.class, 864L)).thenReturn(commandLine);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);

        assertThatJson(json).isEqualTo("{}");
    }

    @Test
    public void execute_should_return_lazy_links_inside_members() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_ID, 1983L);
        parameters.put(GetBusinessDataByIdCommand.ENTITY_CLASS_NAME, Client.class.getName());
        parameters.put(GetBusinessDataByIdCommand.BUSINESS_DATA_URI_PATTERN, BUSINESSDATA_CLASS_URI_VALUE);

        final Client client = new Client();
        final Address address = new Address();
        final Country country = new Country(1L, 2L, "France");
        address.setPersistenceId(864L);
        address.setCountry(country);

        client.setAddress(address);

        client.getAddresses().add(address);
        client.getAddresses().add(address);
        client.getAddresses().add(address);

        when(bdrService.findById(Client.class, 1983L)).thenReturn(client);

        final String json = (String) command.execute(parameters, tenantServiceAccessor);
        System.out.println(json);

        final String expectedJson = IOUtils.toString(GetBusinessDataByIdCommandTest.class.getResource("/rest/ClientWithLinksInChildren.json"));
        JsonAssert.assertThatJson(json).isEqualTo(expectedJson);
    }

}
