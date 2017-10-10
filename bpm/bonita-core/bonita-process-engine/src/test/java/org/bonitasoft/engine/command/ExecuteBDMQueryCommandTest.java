/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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
 **/
package org.bonitasoft.engine.command;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.engine.command.ExecuteBDMQueryCommandTest.ParametersBuilder.parameters;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.operation.pojo.Employee;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteBDMQueryCommandTest {

    @Spy
    private ExecuteBDMQueryCommand command;

    @Mock
    private BusinessDataRepository bdmRepository;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Before
    public void setUp() {
        when(tenantServiceAccessor.getBusinessDataRepository()).thenReturn(bdmRepository);
    }

    @Test
    public void execute_should_return_an_instance() throws Exception {
        // given
        Map<String, Serializable> queryParameters = new HashMap<>();
        when(bdmRepository.findByNamedQuery("a_query", Employee.class, queryParameters))
                .thenReturn(new Employee());
        Map<String, Serializable> parameters = parameters().withQueryName("a_query")
                .withReturnType(Employee.class.getName())
                .withQueryParameters(queryParameters)
                .withIsReturnList(false)
                .build();

        // when
        byte[] result = (byte[]) command.execute(parameters, tenantServiceAccessor);

        // then
        assertThat(new String(result)).isEqualTo(
                "{\"persistenceId\":null,\"persistenceVersion\":null,\"firstName\":null,\"lastName\":null}");
    }

    @Test
    public void execute_should_return_an_instance_when_the_returnsList_parameter_is_not_set() throws Exception {
        // given
        Map<String, Serializable> queryParameters = new HashMap<>();
        when(bdmRepository.findByNamedQuery("a_query", Employee.class, queryParameters))
                .thenReturn(new Employee());
        Map<String, Serializable> parameters = parameters().withQueryName("a_query")
                .withReturnType(Employee.class.getName())
                .withQueryParameters(queryParameters)
                .build();

        // when
        byte[] result = (byte[]) command.execute(parameters, tenantServiceAccessor);

        // then
        assertThat(new String(result)).isEqualTo(
                "{\"persistenceId\":null,\"persistenceVersion\":null,\"firstName\":null,\"lastName\":null}");
    }

    @Test
    public void execute_should_return_an_list() throws Exception {
        // given
        Map<String, Serializable> queryParameters = new HashMap<>();
        when(bdmRepository.findListByNamedQuery("a_query", Employee.class, queryParameters, 0, 10))
                .thenReturn(asList(new Employee(1L, 0L, "Walter", "Bates"), new Employee(2L, 0L, "Helen", "Kelly")));
        Map<String, Serializable> parameters = parameters().withQueryName("a_query")
                .withReturnType(Employee.class.getName())
                .withQueryParameters(queryParameters)
                .withIsReturnList(true)
                .withStartIndex(0)
                .withMaxResults(10)
                .build();

        // when
        byte[] result = (byte[]) command.execute(parameters, tenantServiceAccessor);

        // then
        assertThat(new String(result)).as("json result").isEqualTo(
                "[{\"persistenceId\":1,\"persistenceVersion\":0,\"firstName\":\"Walter\",\"lastName\":\"Bates\"}"
                        + ",{\"persistenceId\":2,\"persistenceVersion\":0,\"firstName\":\"Helen\",\"lastName\":\"Kelly\"}]");
    }

    @Test
    public void execute_should_fail_if_the_query_name_is_null() throws Exception {
        // given
        Map<String, Serializable> parameters = parameters().withQueryName(null)
                .build();

        // when
        Throwable thrown = catchThrowable(() -> command.execute(parameters, tenantServiceAccessor));

        // then
        assertThat(thrown).isInstanceOf(SCommandParameterizationException.class)
                .hasMessageContaining("queryName");
    }

    @Test
    public void execute_should_fail_if_the_return_type_is_empty() throws Exception {
        // given
        Map<String, Serializable> parameters = parameters().withQueryName("a_query")
                .withReturnType(null)
                .build();

        // when
        Throwable thrown = catchThrowable(() -> command.execute(parameters, tenantServiceAccessor));

        // then
        assertThat(thrown).isInstanceOf(SCommandParameterizationException.class)
                .hasMessageContaining("returnType");
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    static class ParametersBuilder {

        private Map<String, Serializable> parameters = new HashMap<>();

        private ParametersBuilder() {
            // hidden
        }

        public static ParametersBuilder parameters() {
            return new ParametersBuilder();
        }

        private ParametersBuilder withParameter(String name, Serializable value) {
            parameters.put(name, value);
            return this;
        }

        public ParametersBuilder withQueryName(String name) {
            return withParameter(ExecuteBDMQueryCommand.QUERY_NAME, name);
        }

        public ParametersBuilder withReturnType(String className) {
            return withParameter(ExecuteBDMQueryCommand.RETURN_TYPE, className);
        }

        public ParametersBuilder withQueryParameters(Map<String, Serializable> queryParameters) {
            return withParameter(ExecuteBDMQueryCommand.QUERY_PARAMETERS, (Serializable) queryParameters);
        }

        public ParametersBuilder withIsReturnList(boolean isReturnList) {
            return withParameter(ExecuteBDMQueryCommand.RETURNS_LIST, isReturnList);
        }

        public ParametersBuilder withStartIndex(int startIndex) {
            return withParameter(ExecuteBDMQueryCommand.START_INDEX, startIndex);
        }

        public ParametersBuilder withMaxResults(int maxResults) {
            return withParameter(ExecuteBDMQueryCommand.MAX_RESULTS, maxResults);
        }

        private Map<String, Serializable> build() {
            return new HashMap<>(parameters);
        }

    }

}
