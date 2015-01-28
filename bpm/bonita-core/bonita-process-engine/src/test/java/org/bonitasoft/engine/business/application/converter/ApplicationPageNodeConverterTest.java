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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.importer.ApplicationPageImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Test
    public void toSApplication_page_should_convert_all_fields_set_applicationId_and_return_no_errors_when_custom_page_is_found() throws Exception {
        //given
        final ApplicationPageNode node = new ApplicationPageNode();
        node.setToken("home");
        node.setCustomPage("customPage");

        final long applicationId = 11L;
        final SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);

        final long pageId = 3L;
        final SPage page = mock(SPage.class);
        given(page.getId()).willReturn(pageId);
        given(pageService.getPageByName("customPage")).willReturn(page);

        //when
        final ApplicationPageImportResult importResult = converter.toSApplicationPage(node, application);

        //then
        assertThat(importResult).isNotNull();
        final SApplicationPage applicationPage = importResult.getApplicationPage();
        assertThat(applicationPage.getToken()).isEqualTo("home");
        assertThat(applicationPage.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationPage.getPageId()).isEqualTo(pageId);

        assertThat(importResult.getError()).isNull();
    }

    @Test
    public void toSApplication_page_should_convert_available_fields_set_applicationId_and_return_errors_when_custom_page_is_not_found() throws Exception {
        //given
        final ApplicationPageNode node = new ApplicationPageNode();
        node.setToken("home");
        node.setCustomPage("customPage");

        final long applicationId = 11L;
        final SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);

        given(pageService.getPageByName("customPage")).willReturn(null);

        //when
        final ApplicationPageImportResult importResult = converter.toSApplicationPage(node, application);

        //then
        assertThat(importResult).isNotNull();
        final SApplicationPage applicationPage = importResult.getApplicationPage();
        assertThat(applicationPage.getToken()).isEqualTo("home");
        assertThat(applicationPage.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationPage.getPageId()).isEqualTo(0);

        assertThat(importResult.getError()).isEqualTo(new ImportError("customPage", ImportError.Type.PAGE));
    }

}
