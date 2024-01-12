/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.parameter.ParameterService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterBusinessArchiveArtifactManagerTest {

    @Mock
    ParameterService parameterService;

    @InjectMocks
    ParameterBusinessArchiveArtifactManager parameterArtifactManager;

    @Test
    public void should_resolve_process_if_all_existing_parameters_have_value() throws Exception {
        // given:
        final BusinessArchive businessArchive = mock(BusinessArchive.class);
        final Map<String, String> providedParametersWithValues = Collections.singletonMap("pName", "someDefaultValue");
        doReturn(providedParametersWithValues).when(businessArchive).getParameters();

        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        final SParameterDefinition sParameterDefinition = mock(SParameterDefinition.class);
        doReturn("pName").when(sParameterDefinition).getName();
        doReturn(Collections.singleton(sParameterDefinition)).when(processDefinition).getParameters();

        // when:
        final boolean deployed = parameterArtifactManager.deploy(businessArchive, processDefinition);

        // then:
        assertThat(deployed).isTrue();
    }

    @Test
    public void resolution_should_ignore_undefined_parameters() throws Exception {
        // given:
        final BusinessArchive businessArchive = mock(BusinessArchive.class);
        final Map<String, String> providedParametersWithValues = new HashMap<>();
        providedParametersWithValues.put("pName", "someDefaultValue");
        providedParametersWithValues.put("ignoredParameter", null);
        doReturn(providedParametersWithValues).when(businessArchive).getParameters();

        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        final SParameterDefinition sParameterDefinition = mock(SParameterDefinition.class);
        doReturn("pName").when(sParameterDefinition).getName();
        doReturn(Collections.singleton(sParameterDefinition)).when(processDefinition).getParameters();

        // when:
        final boolean deployed = parameterArtifactManager.deploy(businessArchive, processDefinition);

        // then:
        assertThat(deployed).isTrue();
    }

    @Test
    public void should_resolve_process_if_no_defined_parameters_in_process() throws Exception {
        // given:
        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        doReturn(Collections.emptySet()).when(processDefinition).getParameters();

        // when:
        final boolean deployed = parameterArtifactManager.deploy(mock(BusinessArchive.class), processDefinition);

        // then:
        assertThat(deployed).isTrue();
        verifyZeroInteractions(parameterService);
    }

    @Test
    public void should_not_resolve_a_null_declared_parameter_value() throws Exception {
        // given:
        final BusinessArchive businessArchive = mock(BusinessArchive.class);
        final Map<String, String> providedParametersWithValues = new HashMap<>();
        providedParametersWithValues.put("pName", null);
        doReturn(providedParametersWithValues).when(businessArchive).getParameters();

        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        final SParameterDefinition sParameterDefinition = mock(SParameterDefinition.class);
        doReturn("pName").when(sParameterDefinition).getName();
        doReturn(Collections.singleton(sParameterDefinition)).when(processDefinition).getParameters();

        // when:
        final boolean deployed = parameterArtifactManager.deploy(businessArchive, processDefinition);

        // then:
        assertThat(deployed).isFalse();
    }

    @Test
    public void should_not_resolve_a_not_provided_declared_parameter_value() throws Exception {
        // given:
        final BusinessArchive businessArchive = mock(BusinessArchive.class);
        doReturn(Collections.emptyMap()).when(businessArchive).getParameters();

        final SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        final SParameterDefinition sParameterDefinition = mock(SParameterDefinition.class);
        doReturn("pName").when(sParameterDefinition).getName();
        doReturn(Collections.singleton(sParameterDefinition)).when(processDefinition).getParameters();

        // when:
        final boolean deployed = parameterArtifactManager.deploy(businessArchive, processDefinition);

        // then:
        assertThat(deployed).isFalse();
    }
}
