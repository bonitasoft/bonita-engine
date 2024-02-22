/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
import org.junit.Test;

public class ApplicationMenuSearchDescriptorConverterTest {

    private final ApplicationMenuSearchDescriptorConverter converter = new ApplicationMenuSearchDescriptorConverter();

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_id_on_convert_attribute_id() throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.ID);
    }

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_display_name_on_convert_attribute_display_name()
            throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.DISPLAY_NAME);
    }

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_applicationPageId_on_convert_attribute_applicationPageId()
            throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.APPLICATION_PAGE_ID);
    }

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_applicationId_on_convert_attribute_applicationId()
            throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_APPLICATION_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.APPLICATION_ID);
    }

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_index_on_convert_attribute_menu_index() throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.INDEX);
    }

    @Test
    public void should_return_ApplicationMenuSearchDescriptor_index_on_convert_attribute_parent_menu()
            throws Exception {
        //when
        final String convertedValue = converter.convert(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID);

        //then
        assertThat(convertedValue).isEqualTo(ApplicationMenuSearchDescriptor.PARENT_ID);
    }

}
