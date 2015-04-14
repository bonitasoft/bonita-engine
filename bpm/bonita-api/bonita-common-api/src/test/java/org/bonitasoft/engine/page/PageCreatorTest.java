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
package org.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.page.PageCreator.PageField;
import org.junit.Test;

public class PageCreatorTest {

    private static final String ZIP_FILE_NAME = "content.zip";

    private static final String DISPLAY_NAME = "display name";

    private static final String NAME = "page name";

    private static final String DESCRIPTION = "page description";
    public static final long PROCESS_DEFINITION_ID = 123L;

    @Test
    public void should_create_page_with_default_content_type() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, ZIP_FILE_NAME).setDisplayName(DISPLAY_NAME).setDescription(DESCRIPTION);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("should set content type").containsOnly(entry(PageField.NAME, NAME),
                entry(PageField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageField.DESCRIPTION, DESCRIPTION),
                entry(PageField.CONTENT_TYPE, ContentType.PAGE),
                entry(PageField.CONTENT_NAME, ZIP_FILE_NAME)
                );

        assertThat(pageCreator.getName()).isEqualTo(NAME);
    }

    @Test
    public void should_create_page_with_process_definition() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, ZIP_FILE_NAME).setDisplayName(DISPLAY_NAME).setDescription(DESCRIPTION)
                .setProcessDefinitionId(PROCESS_DEFINITION_ID).setContentType(ContentType.FORM);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("should set content type").containsOnly(entry(PageField.NAME, NAME),
                entry(PageField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageField.DESCRIPTION, DESCRIPTION),
                entry(PageField.CONTENT_TYPE, ContentType.PAGE),
                entry(PageField.CONTENT_NAME, ZIP_FILE_NAME),
                entry(PageField.CONTENT_TYPE, ContentType.FORM),
                entry(PageField.PROCESS_DEFINITION_ID, PROCESS_DEFINITION_ID)
                );

        assertThat(pageCreator.getName()).isEqualTo(NAME);
    }

    @Test
    public void should_create_page_with_form_content_type() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, ZIP_FILE_NAME, ContentType.FORM, 12345L).setDisplayName(DISPLAY_NAME);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(fields).as("should set content type").containsOnly(
                entry(PageField.NAME, NAME),
                entry(PageField.DISPLAY_NAME, DISPLAY_NAME),
                entry(PageField.CONTENT_TYPE, ContentType.FORM),
                entry(PageField.PROCESS_DEFINITION_ID, 12345L),
                entry(PageField.CONTENT_NAME, ZIP_FILE_NAME)
                );

    }

    @Test
    public void should_print_all_fields() {
        // given
        final PageCreator pageCreator = new PageCreator(NAME, ZIP_FILE_NAME, ContentType.FORM, 12345L).setDisplayName(DISPLAY_NAME).setDescription(
                DESCRIPTION);

        // when
        final Map<PageField, Serializable> fields = pageCreator.getFields();

        // then
        assertThat(pageCreator.toString()).as("should print human readable to string").isEqualTo("PageCreator [fields=" + fields + "]");

    }
}
