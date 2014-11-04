/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.converter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import com.bonitasoft.engine.business.application.impl.converter.ApplicationContainerConverter;
import com.bonitasoft.engine.business.application.impl.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNode;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNodeContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationContainerConverterTest {

    @Mock
    private ApplicationNodeConverter applicationNodeConverter;

    @InjectMocks
    private ApplicationContainerConverter applicationContainerConverter;

    @Test
    public void toNode_should_create_a_container_to_put_all_converted_applications() throws Exception {
        //given
        SApplication app1 = mock(SApplication.class);
        SApplication app2 = mock(SApplication.class);

        ApplicationNode appNode1 = mock(ApplicationNode.class);
        ApplicationNode appNode2 = mock(ApplicationNode.class);

        given(applicationNodeConverter.toNode(app1)).willReturn(appNode1);
        given(applicationNodeConverter.toNode(app2)).willReturn(appNode2);

        //when
        ApplicationNodeContainer nodeContainer = applicationContainerConverter.toNode(Arrays.asList(app1, app2));

        //then
        assertThat(nodeContainer).isNotNull();
        assertThat(nodeContainer.getApplications()).containsExactly(appNode1, appNode2);
    }


}