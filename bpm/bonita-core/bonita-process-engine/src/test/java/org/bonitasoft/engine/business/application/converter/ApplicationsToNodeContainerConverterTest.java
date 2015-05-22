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
package org.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsToNodeContainerConverterTest {

    @Mock
    private ApplicationToNodeConverter applicationToNodeConverter;

    @InjectMocks
    private ApplicationsToNodeContainerConverter applicationsToNodeContainerConverter;

    @Test
    public void toNode_should_create_a_container_to_put_all_converted_applications() throws Exception {
        //given
        SApplication app1 = mock(SApplication.class);
        SApplication app2 = mock(SApplication.class);

        ApplicationNode appNode1 = mock(ApplicationNode.class);
        ApplicationNode appNode2 = mock(ApplicationNode.class);

        given(applicationToNodeConverter.toNode(app1)).willReturn(appNode1);
        given(applicationToNodeConverter.toNode(app2)).willReturn(appNode2);

        //when
        ApplicationNodeContainer nodeContainer = applicationsToNodeContainerConverter.toNode(Arrays.asList(app1, app2));

        //then
        assertThat(nodeContainer).isNotNull();
        assertThat(nodeContainer.getApplications()).containsExactly(appNode1, appNode2);
    }

}
