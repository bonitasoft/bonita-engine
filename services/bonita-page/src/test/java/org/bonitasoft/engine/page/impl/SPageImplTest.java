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
package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SPage;
import org.junit.Test;

public class SPageImplTest {

    private static final String DESCRIPTION = "description";

    private static final boolean PROVIDED = true;

    private static final String NAME = "name";

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
    public void should_set_all_fields() {
        //given
        final SPage sPage = new SPage(NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED, CONTENT_ZIP);
        sPage.setTenantId(TENANT_ID);
        sPage.setId(ID);
        sPage.setDisplayName(DISPLAY_NAME);
        sPage.setDescription(DESCRIPTION);
        sPage.setLastModificationDate(LAST_MODIFICATION_DATE);
        sPage.setLastUpdatedBy(LAST_UPDATED_BY);

        //when then
        SPageImplAssert.assertThat(sPage)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .isProvided()
                .hasDescription(DESCRIPTION)
                .hasInstallationDate(INSTALLATION_DATE)
                .hasLastModificationDate(LAST_MODIFICATION_DATE)
                .hasContentName(CONTENT_ZIP)
                .hasContentType(SContentType.PAGE)
                .hasLastUpdatedBy(LAST_UPDATED_BY)
                .hasInstalledBy(INSTALLED_BY)
                .hasId(ID)
                .hasTenantId(TENANT_ID);

    }

    @Test
    public void should_set_all_fields_from_sPage() {
        //given
        final SPage sPage = new SPage(NAME, DESCRIPTION, DISPLAY_NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED,
                LAST_MODIFICATION_DATE,
                LAST_UPDATED_BY,
                CONTENT_ZIP);

        //when
        SPage sPage2 = new SPage(sPage);

        //then
        assertThat(sPage2).isEqualToComparingFieldByField(sPage);
    }

    @Test
    public void should_set_content_type_and_process_definition() {
        //given
        final SPage sPage = new SPage(NAME, DESCRIPTION, DISPLAY_NAME, INSTALLATION_DATE, INSTALLED_BY, PROVIDED,
                LAST_MODIFICATION_DATE,
                LAST_UPDATED_BY,
                CONTENT_ZIP);

        //when
        sPage.setProcessDefinitionId(PROCESS_DEFINITION_ID);
        sPage.setContentType(SContentType.FORM);

        //then
        SPageImplAssert.assertThat(sPage)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .hasContentType(SContentType.FORM);
        assertThat(sPage.toString()).contains(String.valueOf(PROCESS_DEFINITION_ID)).contains(SContentType.FORM);

    }
}
