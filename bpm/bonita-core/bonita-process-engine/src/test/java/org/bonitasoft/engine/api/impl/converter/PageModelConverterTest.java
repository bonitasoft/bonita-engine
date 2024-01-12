/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SPage;
import org.junit.Test;

public class PageModelConverterTest {

    private static final String ZIP_FILE_NAME = "content.zip";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "description";

    private static final boolean PROVIDED = true;

    private static final String DISPLAY_NAME = "display name";

    public static final long PROCESS_DEFINITION_ID = 456789L;
    public static final long INSTALLATION_DATE = 1L;
    public static final long LAST_MODIFICATION_DATE = 2L;
    public static final String CONTENT_ZIP = "content.zip";
    private static final long LAST_UPDATED_BY = 3L;
    public static final long INSTALLED_BY = 4L;
    public static final long TENANT_ID = 5L;
    public static final long ID = 6L;

    @Test
    public void should_construct_SPage_with_pageCreator() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final PageCreator pageCreator = new PageCreator(NAME, ZIP_FILE_NAME, ContentType.FORM, PROCESS_DEFINITION_ID)
                .setDisplayName(DISPLAY_NAME)
                .setDescription(DESCRIPTION);

        //when
        final SPage sPage = pageModelConverter.constructSPage(pageCreator, LAST_UPDATED_BY);

        //then
        SPageAssert.assertThat(sPage)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .hasDescription(DESCRIPTION)
                .hasContentName(ZIP_FILE_NAME)
                .hasContentType(SContentType.FORM)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .isNotProvided()
                .hasLastUpdatedBy(LAST_UPDATED_BY);
    }

    @Test
    public void should_construct_SPage_with_pageUpdater() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final PageUpdater pageUpdater = new PageUpdater().setName(NAME).setContentName(ZIP_FILE_NAME)
                .setDisplayName(DISPLAY_NAME).setDescription(DESCRIPTION)
                .setContentType(ContentType.PAGE).setProcessDefinitionId(PROCESS_DEFINITION_ID);

        //when
        final SPage sPage = pageModelConverter.constructSPage(pageUpdater, LAST_UPDATED_BY);

        //then
        SPageAssert.assertThat(sPage)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .hasDescription(DESCRIPTION)
                .hasContentName(ZIP_FILE_NAME)
                .hasContentType(SContentType.PAGE)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .isNotProvided()
                .hasLastUpdatedBy(LAST_UPDATED_BY);

    }

    @Test
    public void should_constructSPage_with_null_processDefinitionId_set_it_to_0() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final PageUpdater pageUpdater = new PageUpdater().setName(NAME).setContentName(ZIP_FILE_NAME)
                .setDisplayName(DISPLAY_NAME).setDescription(DESCRIPTION)
                .setContentType(ContentType.PAGE).setProcessDefinitionId(null);

        //when
        final SPage sPage = pageModelConverter.constructSPage(pageUpdater, LAST_UPDATED_BY);

        //then
        SPageAssert.assertThat(sPage).hasName(NAME).hasProcessDefinitionId(0);
    }

    @Test
    public void should_construct_Page_with_SPage() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final SPage sPage = new SPage(NAME, DESCRIPTION, DISPLAY_NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED,
                LAST_MODIFICATION_DATE,
                LAST_UPDATED_BY,
                CONTENT_ZIP);
        sPage.setContentType(SContentType.FORM);
        sPage.setProcessDefinitionId(PROCESS_DEFINITION_ID);

        //when
        final Page page = pageModelConverter.toPage(sPage);

        //then
        PageAssert.assertThat(page)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .hasDescription(DESCRIPTION)
                .hasContentName(ZIP_FILE_NAME)
                .hasContentType(ContentType.FORM)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .isProvided()
                .hasLastUpdatedBy(LAST_UPDATED_BY);

    }

    @Test
    public void should_construct_Page_with_SPage_with_no_processDefinitionId() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final SPage sPage = new SPage(NAME, DESCRIPTION, DISPLAY_NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED,
                LAST_MODIFICATION_DATE,
                LAST_UPDATED_BY,
                CONTENT_ZIP);
        //when
        final Page page = pageModelConverter.toPage(sPage);
        //then
        assertThat(page.getProcessDefinitionId()).isNull();
    }

    @Test
    public void should_construct_Page_with_SPage_list() {
        //given
        PageModelConverter pageModelConverter = new PageModelConverter();
        final SPage sPage = new SPage(NAME, DESCRIPTION, DISPLAY_NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED,
                LAST_MODIFICATION_DATE,
                LAST_UPDATED_BY,
                CONTENT_ZIP);
        List<SPage> sPages = new ArrayList<>();
        sPages.add(sPage);

        //when
        final Page page = pageModelConverter.toPage(sPage);
        final List<Page> pages = pageModelConverter.toPages(sPages);

        //then
        assertThat(pages).hasSize(1);
        assertThat(pages.get(0)).isEqualToComparingFieldByField(page);

    }
}
