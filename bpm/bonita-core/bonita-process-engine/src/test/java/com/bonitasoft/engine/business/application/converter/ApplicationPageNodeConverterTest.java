/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.SPage;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageNodeConverterTest {

    @Mock
    PageService pageService;

    @InjectMocks
    private ApplicationPageNodeConverter converter;

    @Test(expected = IllegalArgumentException.class)
    public void convertNullPageShouldThrowIllegalArgument() throws Exception {
        converter.toPage(null);
    }

    @Test
    public void convertPageShouldConvertAllFields() throws Exception {
        // given:
        final String customPage = "customPage";
        final String token = "tekken";
        final long applicationId = 38L;
        final long pageId = 154L;
        final ApplicationPageNode node = new ApplicationPageNode();
        node.setCustomPage(customPage);
        node.setToken(token);
        final SPage sPage = mock(SPage.class);
        doReturn(sPage).when(pageService).getPage(pageId);
        doReturn(customPage).when(sPage).getName();

        // when:
        final ApplicationPageNode convertedPage = converter.toPage(new SApplicationPageImpl(applicationId, pageId, token));

        // then:
        assertThat(convertedPage.getCustomPage()).isEqualTo(customPage);
        assertThat(convertedPage.getToken()).isEqualTo(token);
    }
}
