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
package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.bonitasoft.engine.page.ContentType;
import org.junit.Test;

public class PageImplTest {

    private static final long USER_ID = 1l;

    private static final String DESCRIPTION = "description";

    private static final boolean PROVIDED = true;

    private static final String NAME = "name";

    private static final String DISPLAY_NAME = "display name";

    public static final long PROCESS_DEFINITION_ID = 456789L;

    @Test
    public void should_set_all_fields() {

        Date date = new Date(1);
        Date modificationDate = new Date(2);

        PageImplAssert
                .assertThat(
                        new PageImpl(-1l, NAME, DISPLAY_NAME, PROVIDED, DESCRIPTION, date.getTime(), USER_ID, modificationDate.getTime(), USER_ID,
                                "content.zip", ContentType.PAGE, null))
                .hasId(-1l)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .isProvided()
                .hasDescription(DESCRIPTION).hasInstallationDate(date)
                .hasLastModificationDate(modificationDate)
                .hasContentType(ContentType.PAGE);

    }

    @Test
    public void should_set_all_form_fields() {

        Date date = new Date(1);
        Date modificationDate = new Date(2);

        PageImplAssert
                .assertThat(
                        new PageImpl(-1l, NAME, DISPLAY_NAME, PROVIDED, DESCRIPTION, date.getTime(), USER_ID, modificationDate.getTime(), USER_ID,
                                "content.zip", ContentType.FORM, PROCESS_DEFINITION_ID))
                .hasId(-1l)
                .hasName(NAME)
                .hasDisplayName(DISPLAY_NAME)
                .isProvided()
                .hasDescription(DESCRIPTION).hasInstallationDate(date)
                .hasLastModificationDate(modificationDate)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .hasContentType(ContentType.FORM);

    }

    @Test
    public void should_display_all_fields_in_to_string() {

        Date date = new Date(1);
        Date modificationDate = new Date(2);

        assertThat(
                new PageImpl(-1l, NAME, DISPLAY_NAME, PROVIDED, DESCRIPTION, date.getTime(), USER_ID, modificationDate.getTime(), USER_ID,
                        "content.zip", ContentType.FORM, PROCESS_DEFINITION_ID).toString()).contains(String.valueOf(PROCESS_DEFINITION_ID)).contains(
                ContentType.FORM);

    }
}
