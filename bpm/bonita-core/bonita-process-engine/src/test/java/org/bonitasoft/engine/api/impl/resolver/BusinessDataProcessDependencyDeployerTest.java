/**
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
 **/
package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SBusinessDataDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataProcessDependencyDeployerTest {

    @Mock
    private TenantServiceAccessor tenantAccessor;

    @Mock
    private BusinessDataRepository businessDataRepository;

    private BusinessDataProcessDependencyDeployer resolver;

    @Before
    public void setUp() {
        resolver = new BusinessDataProcessDependencyDeployer();
        final Set<String> entityClassNames = new HashSet<String>();
        entityClassNames.add("com.bonitasoft.Employee");
        entityClassNames.add("com.bonitasoft.LeaveRequest");

        when(tenantAccessor.getBusinessDataRepository()).thenReturn(businessDataRepository);
        when(businessDataRepository.getEntityClassNames()).thenReturn(entityClassNames);
    }

    private SProcessDefinition buildProcessDefinition(final SBusinessDataDefinition... businessDataDefinitions) {
        final SFlowElementContainerDefinitionImpl processContainer = new SFlowElementContainerDefinitionImpl();
        final SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("process", "1.0");
        processDefinition.setProcessContainer(processContainer);
        for (final SBusinessDataDefinition sBusinessDataDefinition : businessDataDefinitions) {
            processContainer.addBusinessDataDefinition(sBusinessDataDefinition);
        }
        return processDefinition;
    }

    private SBusinessDataDefinition buildBusinessDataDefinition(final String name, final String className) {
        final SBusinessDataDefinitionImpl businessDataDefinition = new SBusinessDataDefinitionImpl();
        businessDataDefinition.setName(name);
        businessDataDefinition.setClassName(className);
        return businessDataDefinition;
    }

    @Test
    public void checkResolution_returns_no_problem_with_no_business_data() {
        final SProcessDefinition processDefinition = buildProcessDefinition();

        final List<Problem> problems = resolver.checkResolution(tenantAccessor, processDefinition);

        assertThat(problems).isEmpty();
    }

    @Test
    public void checkResolution_returns_no_problem_with_a_valid_business_data() {
        final SProcessDefinition processDefinition = buildProcessDefinition(buildBusinessDataDefinition("bizData", "com.bonitasoft.Employee"));

        final List<Problem> problems = resolver.checkResolution(tenantAccessor, processDefinition);

        assertThat(problems).isEmpty();
    }

    @Test
    public void checkResolution_returns_a_problem_with_invalid_business_data() {
        final SProcessDefinition processDefinition = buildProcessDefinition(buildBusinessDataDefinition("bizData1", "com.bonitasoft.Address"),
                buildBusinessDataDefinition("bizData2", Long.class.getName()));

        final List<Problem> problems = resolver.checkResolution(tenantAccessor, processDefinition);

        assertThat(problems).areExactly(2, new ProblemCondition());
    }

}
