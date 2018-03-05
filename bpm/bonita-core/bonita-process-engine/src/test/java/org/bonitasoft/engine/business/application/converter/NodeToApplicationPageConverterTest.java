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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.importer.ApplicationPageImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeToApplicationPageConverterTest {

    @Mock
    PageService pageService;

    @Mock
    ApplicationImportValidator validator;

    @InjectMocks
    private NodeToApplicationPageConverter converter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static long APPLICATION_ID = 11L;

    private SApplication application;

    @Before
    public void setUp() throws Exception {
        application = new SApplicationImpl();
        application.setId(APPLICATION_ID);
    }

    @Test
    public void toSApplication_page_should_convert_all_fields_set_applicationId_and_return_no_errors_when_custom_page_is_found() throws Exception {
        //given
        final ApplicationPageNode node = buildAppPageNode("home", "customPage");

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
        assertThat(applicationPage.getApplicationId()).isEqualTo(APPLICATION_ID);
        assertThat(applicationPage.getPageId()).isEqualTo(pageId);

        assertThat(importResult.getError()).isNull();
    }

    @Test
    public void toSApplication_page_should_convert_available_fields_set_applicationId_and_return_errors_when_custom_page_is_not_found() throws Exception {
        //given
        final ApplicationPageNode node = buildAppPageNode("home", "customPage");

        given(pageService.getPageByName("customPage")).willReturn(null);

        //when
        final ApplicationPageImportResult importResult = converter.toSApplicationPage(node, application);

        //then
        assertThat(importResult).isNotNull();
        final SApplicationPage applicationPage = importResult.getApplicationPage();
        assertThat(applicationPage.getToken()).isEqualTo("home");
        assertThat(applicationPage.getApplicationId()).isEqualTo(APPLICATION_ID);
        assertThat(applicationPage.getPageId()).isEqualTo(0);

        assertThat(importResult.getError()).isEqualTo(new ImportError("customPage", ImportError.Type.PAGE));
    }

    private ApplicationPageNode buildAppPageNode(final String token, final String customPageName) {
        final ApplicationPageNode node = new ApplicationPageNode();
        node.setToken(token);
        node.setCustomPage(customPageName);
        return node;
    }

    @Test
    public void toSApplicationPage_should_throw_ImportException_when_page_has_invalid_token() throws Exception {
        //given
        ApplicationPageNode pageNode = buildAppPageNode("invalid", "page");
        doThrow(new ImportException("invalid token")).when(validator).validate("invalid");

        //then
        expectedException.expect(ImportException.class);
        expectedException.expectMessage("invalid token");

        //when
        converter.toSApplicationPage(pageNode, application);

    }
}
