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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageToNodeConverterTest {

    @Mock
    PageService pageService;

    @InjectMocks
    private ApplicationPageToNodeConverter converter;

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
