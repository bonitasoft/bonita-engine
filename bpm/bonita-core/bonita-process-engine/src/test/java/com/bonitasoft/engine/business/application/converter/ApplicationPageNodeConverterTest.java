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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.bonitasoft.engine.business.application.importer.ApplicationPageImportResult;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.api.ImportError;
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

    @Test
    public void toSApplication_page_should_convert_all_fields_set_applicationId_and_return_no_errors_when_custom_page_is_found() throws Exception {
        //given
        ApplicationPageNode node = new ApplicationPageNode();
        node.setToken("home");
        node.setCustomPage("customPage");

        long applicationId = 11L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);

        long pageId = 3L;
        SPage page = mock(SPage.class);
        given(page.getId()).willReturn(pageId);
        given(pageService.getPageByName("customPage")).willReturn(page);

        //when
        ApplicationPageImportResult importResult = converter.toSApplicationPage(node, application);

        //then
        assertThat(importResult).isNotNull();
        SApplicationPage applicationPage = importResult.getApplicationPage();
        assertThat(applicationPage.getToken()).isEqualTo("home");
        assertThat(applicationPage.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationPage.getPageId()).isEqualTo(pageId);

        assertThat(importResult.getError()).isNull();
    }

    @Test
    public void toSApplication_page_should_convert_available_fields_set_applicationId_and_return_errors_when_custom_page_is_not_found() throws Exception {
        //given
        ApplicationPageNode node = new ApplicationPageNode();
        node.setToken("home");
        node.setCustomPage("customPage");

        long applicationId = 11L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);

        given(pageService.getPageByName("customPage")).willReturn(null);

        //when
        ApplicationPageImportResult importResult = converter.toSApplicationPage(node, application);

        //then
        assertThat(importResult).isNotNull();
        SApplicationPage applicationPage = importResult.getApplicationPage();
        assertThat(applicationPage.getToken()).isEqualTo("home");
        assertThat(applicationPage.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationPage.getPageId()).isEqualTo(0);

        assertThat(importResult.getError()).isEqualTo(new ImportError("customPage", ImportError.Type.PAGE));
    }

}
